package mrp_v2.biomeborderviewer.mixin;

import mrp_v2.biomeborderviewer.client.renderer.debug.VisualizeBorders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At(value = "HEAD"), method = "setWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    public void setWorldMixin(ClientWorld world, CallbackInfo ci) {
        VisualizeBorders.worldUnload(world);
    }
}
