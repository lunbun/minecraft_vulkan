package io.github.lunbun.quasar.mixin.vulkan.lwjgl;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.lunbun.quasar.client.glsim.GlTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlStateManager.class)
public class GL11Textures {
    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGenTextures()I"), method = "genTextures")
    private static int genTextures() {
        return GlTextures.genTexture();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"), method = "bindTexture")
    private static void bindTexture(int target, int texture) {
        GlTextures.bindTexture(target, texture);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexParameteri(III)V"), method = "texParameter(III)V")
    private static void bindTexture(int target, int pname, int param) {
        GlTextures.texParameteri(target, pname, param);
    }
}
