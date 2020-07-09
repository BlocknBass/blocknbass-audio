package nl.blocknbass.audio;

import net.fabricmc.api.ModInitializer;

public class BlocknBassAudio implements ModInitializer {

    public static BlocknBassAudio INSTANCE;
    public AudioPlayer player;

    @Override
    public void onInitialize() {
        System.out.println("Initializing Block & Bass Audio!");
        INSTANCE = this;
    }
}
