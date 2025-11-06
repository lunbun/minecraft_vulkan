package io.github.lunbun.quasar.mixin.vulkan.blaze3d;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lunbun.quasar.Quasar;
import io.github.lunbun.quasar.client.render.QuasarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    @Shadow public static void assertThread(Supplier<Boolean> check) {
        throw new UnsupportedOperationException();
    }

    /**
     * @author Lunbun
     * @reason Redirect renderer initializer to initialize Vulkan instead
     */
    @Overwrite
    public static void initRenderer(int debugVerbosity, boolean debugSync) {
        assertThread(RenderSystem::isInInitPhase);
        QuasarRenderer.initVulkan();
    }

    /**
     * @author Lunbun
     * @reason Remove OpenGL calls
     */
    @Overwrite
    public static void enableBlend() {
        Quasar.LOGGER.warn("Tried to enable blend!");
    }

    /**
     * @author Lunbun
     * @reason Remove OpenGL calls
     */
    @Overwrite
    public static void disableBlend() {
        Quasar.LOGGER.warn("Tried to disable blend!");
    }

    /**
     * @author Lunbun
     * @reason Remove OpenGL calls
     */
    @Overwrite
    public static void disableTexture() {
        Quasar.LOGGER.warn("Tried to disable texture!");
    }

    /**
     * @author Lunbun
     * @reason Remove OpenGL calls
     */
    @Overwrite
    public static void enableTexture() {
        Quasar.LOGGER.warn("Tried to enable texture!");
    }

    /**
     * @author Lunbun
     * @reason Remove OpenGL calls
     */
    @Overwrite
    public static void blendFuncSeparate(GlStateManager.SrcFactor srcFactor, GlStateManager.DstFactor dstFactor, GlStateManager.SrcFactor srcAlpha, GlStateManager.DstFactor dstAlpha) {
        Quasar.LOGGER.warn("Tried to change blend function!");
    }
}
