package io.github.lunbun.quasar.client.glsim;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;

public class GlTextures {
    private static int boundTexture;

    public static int genTexture() {
        return GlNames.genName();
    }

    public static void bindTexture(int target, int texture) {
        if (target != GL11.GL_TEXTURE_2D) {
            throw new RuntimeException("Texture is not 2d");
        }

        boundTexture = texture;
    }

    public static void texParameteri(int target, int pname, int param) {
        if (target != GL11.GL_TEXTURE_2D) {
            throw new RuntimeException("Texture is not 2d");
        }

        MinecraftClient
        if (pname == GL12.GL_TEXTURE_MAX_LEVEL) {
            // TODO
        } else {
            GL11.glTexParameteri(target, pname, param);
        }
    }

    public static void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type) {
        throw new UnsupportedOperationException("ti2d");
    }
}
