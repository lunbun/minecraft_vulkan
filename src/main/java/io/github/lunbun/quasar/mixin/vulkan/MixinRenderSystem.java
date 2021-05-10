package io.github.lunbun.quasar.mixin.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
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
}
