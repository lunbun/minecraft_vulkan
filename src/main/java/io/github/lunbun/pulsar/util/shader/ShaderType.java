package io.github.lunbun.pulsar.util.shader;

import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.VK10;

public enum ShaderType {
    VERTEX_SHADER(Shaderc.shaderc_glsl_vertex_shader, VK10.VK_SHADER_STAGE_VERTEX_BIT),
    GEOMETRY_SHADER(Shaderc.shaderc_glsl_geometry_shader, VK10.VK_SHADER_STAGE_GEOMETRY_BIT),
    FRAGMENT_SHADER(Shaderc.shaderc_glsl_fragment_shader, VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

    public final int type;
    public final int bits;

    ShaderType(int type, int bits) {
        this.type = type;
        this.bits = bits;
    }
}
