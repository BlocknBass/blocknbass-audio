package nl.blocknbass.audio;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class AudioPlayer implements Runnable {
    private Thread thread;
    private String url;
    private IntBuffer source, buffer;
    boolean playing;

    public AudioPlayer(String url) {
        System.out.println("Starting stream with url " + url);
        this.url = url;
        thread = new Thread(this);
        thread.start();
    }

    private boolean alError() {
        if (AL10.alGetError() != AL10.AL_NO_ERROR) {
            System.err.println(String.format("[Block & Bass Audio] AL10 error %d: %s",
                    AL10.alGetError(), AL10.alGetString(AL10.alGetError())));
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = null;
        try {
            request = new Request.Builder().url(url).build();
        } catch (IllegalArgumentException e) {
            url = null;
            System.err.println("[Block & Bass Audio] Failed to build http request for url "
            + url);
            e.printStackTrace();
            return;
        }

        Response response = null;
        AudioInputStream audioInputStream = null;
        try {
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            url = null;
            System.err.println("[Block & Bass Audio] Failed to execute http request for url "
                    + url);
            e.printStackTrace();
            return;
        }
        System.out.println("Content type: " + response.header("Content-type", "unknown"));

        InputStream bis;
        try {
            bis = new MarkErrorInputStream(new BufferedInputStream(response.body().byteStream()));
            audioInputStream = AudioSystem.getAudioInputStream(bis);
        } catch (IOException | UnsupportedAudioFileException e) {
            url = null;
            System.err.println("[Block & Bass Audio] Failed to get audio stream for url "
                    + url);
            e.printStackTrace();
            return;
        }

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        this.source = BufferUtils.createIntBuffer(1);
        AL10.alGenSources(source);
        if (alError()) {
            close();
            return;
        }

        AL10.alSourcei(this.source.get(0), AL10.AL_LOOPING, AL10.AL_FALSE);
        AL10.alSourcef(this.source.get(0), AL10.AL_PITCH, 1.0f);
        AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, 9.0f * MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS));

        this.playing = true;
        try {
            stream(AudioSystem.getAudioInputStream(format, audioInputStream));
        } catch (IOException e) {
            url = null;
            System.err.println("[Block & Bass Audio] IOException on url "
                    + url);
            e.printStackTrace();
            return;
        }

        if (this.playing) {
            while (AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {e.printStackTrace();}
            }
        }

        close();
    }

    private void stream(AudioInputStream in) throws IOException {
        AudioFormat format = in.getFormat();

        byte[] databuffer = new byte[65536];
        for (int n = 0; this.playing && n != -1; n = in.read(databuffer, 0, databuffer.length)) {
            if (n <= 0)
                continue;

            if (this.buffer == null) {
                this.buffer = BufferUtils.createIntBuffer(1);
            } else {
                int processed = AL10.alGetSourcei(this.source.get(0), AL10.AL_BUFFERS_PROCESSED);
                if (processed > 0) {
                    AL10.alSourceUnqueueBuffers(this.source.get(0), this.buffer);
                    alError();
                }
            }

            AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, 9.0f * MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS));
            AL10.alGenBuffers(this.buffer);
            ByteBuffer data = (ByteBuffer) BufferUtils.createByteBuffer(n).order(ByteOrder.LITTLE_ENDIAN).put(databuffer, 0, n).flip();
            AL10.alBufferData(this.buffer.get(0), format.getChannels() > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16,
                    data, (int)format.getSampleRate());
            alError();
            AL10.alSourceQueueBuffers(this.source.get(0), buffer);

            int state = AL10.alGetSourcei(this.source.get(0), AL10.AL_SOURCE_STATE);
            if (this.playing && state != AL10.AL_PLAYING)
                AL10.alSourcePlay(this.source.get(0));
        }
    }

    private void close() {
        this.playing = false;
        if (this.source != null) {
            AL10.alSourceStopv(this.source);
            AL10.alDeleteSources(this.source);
            this.source = null;
        }

        if (this.buffer != null) {
            AL10.alDeleteBuffers(this.buffer);
            this.buffer = null;
        }
    }

    public void stop() {
        this.playing = false;
        if (this.source != null) {
            AL10.alSourcef(this.source.get(0), AL10.AL_GAIN, 0.0f);
            AL10.alSourceStopv(this.source);
        }
    }
}
