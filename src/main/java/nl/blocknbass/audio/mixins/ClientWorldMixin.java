package nl.blocknbass.audio.mixins;

import net.minecraft.client.world.ClientWorld;
import nl.blocknbass.audio.BlocknBassAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method="disconnect()V", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        if (BlocknBassAudio.INSTANCE.player != null)
            BlocknBassAudio.INSTANCE.player.stop();
    }
}
