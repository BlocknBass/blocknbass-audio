package nl.blocknbass.audio.network;

import com.google.protobuf.InvalidProtocolBufferException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import nl.blocknbass.audio.proto.BuildMessageProto;
import nl.blocknbass.core.BlocknBassPacketHandler;
import nl.blocknbass.core.proto.MessageProto;
import nl.blocknbass.light.proto.LightMessageProto;

import java.util.List;

public class BlocknBassAudioPacketHandler implements BlocknBassPacketHandler {
    @Override
    public void handle(MessageProto.Message message, MinecraftClient minecraftClient) {
    }
}
