package io.github.lunbun.quasar.mixin.vulkan;

import io.github.lunbun.quasar.client.render.QuasarRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow @Final private Window window;

    /**
     * @author Lunbun
     * @reason Redirect to Quasar
     */
    @Overwrite
    public void onResolutionChanged() {
        QuasarRenderer.resizeFramebuffer(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
    }
}
