package nl.blocknbass.audio;

import nl.blocknbass.audio.network.BlocknBassAudioPacketHandler;
import nl.blocknbass.core.BlocknBassPacketDispatcher;
import nl.blocknbass.core.IBlocknBassMod;

public class BlocknBassModAudio implements IBlocknBassMod {
    @Override
    public void registerPacketSet(BlocknBassPacketDispatcher blocknBassPacketDispatcher) {
        blocknBassPacketDispatcher.register("audio", new BlocknBassAudioPacketHandler());
    }
}