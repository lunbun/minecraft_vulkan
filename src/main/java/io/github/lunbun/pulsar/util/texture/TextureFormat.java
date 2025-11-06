package io.github.lunbun.pulsar.util.texture;

import org.lwjgl.vulkan.VK10;

public enum TextureFormat {
    GRAYSCALE(VK10.VK_FORMAT_R8_SRGB, 1),
    RGB(VK10.VK_FORMAT_R8G8B8_SRGB, 3),
    RGBA(VK10.VK_FORMAT_R8G8B8A8_SRGB, 4),
    ABGR(VK10.VK_FORMAT_A8B8G8R8_SRGB_PACK32, 4);

    public final int vulkan;
    public final int channels;

    TextureFormat(int vulkan, int channels) {
        this.vulkan = vulkan;
        this.channels = channels;
    }
}
