package nl.blocknbass.audio.network;

import com.google.protobuf.InvalidProtocolBufferException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.text.LiteralText;
import nl.blocknbass.audio.AudioPlayer;
import nl.blocknbass.audio.BlocknBassAudio;
import nl.blocknbass.audio.proto.AudioMessageProto;
import nl.blocknbass.audio.proto.BuildMessageProto;
import nl.blocknbass.core.BlocknBassPacketHandler;
import nl.blocknbass.core.proto.MessageProto;
import nl.blocknbass.light.proto.LightMessageProto;

import java.util.List;

public class BlocknBassAudioPacketHandler implements BlocknBassPacketHandler {
    @Override
    public void handle(MessageProto.Message message, MinecraftClient minecraftClient) {
        AudioMessageProto.AudioMessage audioMessage;
        try {
            audioMessage = message.getMessage().unpack(
                    AudioMessageProto.AudioMessage.class);
        } catch (InvalidProtocolBufferException e) {
            System.err.println("Couldn't decode light protobuf!");
            return;
        }

        switch (audioMessage.getType()) {
            case AudioMessageProto.AudioCommand.PLAY_AUDIO_VALUE: {
                if (BlocknBassAudio.INSTANCE.player != null)
                    BlocknBassAudio.INSTANCE.player.stop();

                BlocknBassAudio.INSTANCE.player = new AudioPlayer(audioMessage.getUrl());
                break;
            }
            case AudioMessageProto.AudioCommand.STOP_AUDIO_VALUE: {
                if (BlocknBassAudio.INSTANCE.player != null)
                    BlocknBassAudio.INSTANCE.player.stop();
                break;
            }
        }
    }
}
