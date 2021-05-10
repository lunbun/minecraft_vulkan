package io.github.lunbun.quasar.mixin.vulkan;

import io.github.lunbun.quasar.client.render.QuasarRenderer;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class MixinWindow {
    @Shadow public abstract int getFramebufferWidth();

    @Inject(at = @At(value = "INVOKE", target =
            "Lcom/mojang/blaze3d/systems/RenderSystem;assertThread(Ljava/util/function/Supplier;)V",
            ordinal = 0), method = "<init>")
    private void initWindow(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings,
                            String videoMode, String title, CallbackInfo ci) {
        QuasarRenderer.initWindow();
    }

    @Redirect(at = @At(value = "INVOKE", target =
            "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), method = "<init>")
    private long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        // intercept the window handle
        long handle = GLFW.glfwCreateWindow(width, height, title, monitor, share);
        QuasarRenderer.createWindow(handle);
        return handle;
    }

    @Inject(at = @At(value = "HEAD"), method = "close")
    private void cleanup(CallbackInfo ci) {
        QuasarRenderer.cleanup();
    }
}
