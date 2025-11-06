package io.github.lunbun.pulsar.component.texture;

import io.github.lunbun.pulsar.component.drawing.CommandPool;
import io.github.lunbun.pulsar.component.presentation.ImageViewsManager;
import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import io.github.lunbun.pulsar.component.setup.PhysicalDevice;
import io.github.lunbun.pulsar.component.setup.QueueManager;
import io.github.lunbun.pulsar.component.vertex.MemoryAllocator;
import io.github.lunbun.pulsar.struct.texture.ImageData;
import io.github.lunbun.pulsar.struct.vertex.BufferData;
import io.github.lunbun.pulsar.util.texture.TextureFormat;
import io.github.lunbun.pulsar.util.vulkan.ImageUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class Texture extends ImageData {
    public final long imageView;
    public final int imageSize;
    public final int texWidth;
    public final int texHeight;
    public final TextureFormat format;
    private final Loader loader;

    protected Texture(BufferData bufferData, int imageSize, int texWidth, int texHeight, TextureFormat format,
                      long imageView, Loader loader) {
        super(bufferData);
        this.imageView = imageView;
        this.imageSize = imageSize;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.format = format;
        this.loader = loader;
    }

    public void destroy() {
        this.loader.destroy(this);
    }

    public static final class Loader {
        private final LogicalDevice device;
        private final PhysicalDevice physicalDevice;
        private final MemoryAllocator memoryAllocator;
        private final CommandPool commandPool;
        private final QueueManager queues;

        public Loader(LogicalDevice device, PhysicalDevice physicalDevice, MemoryAllocator memoryAllocator,
                      CommandPool commandPool, QueueManager queues) {
            this.device = device;
            this.physicalDevice = physicalDevice;
            this.memoryAllocator = memoryAllocator;
            this.commandPool = commandPool;
            this.queues = queues;
        }

        public void destroy(Texture texture) {
            ImageViewsManager.destroyImageView(this.device, texture.imageView);
            ImageUtils.destroyImage(this.device, this.memoryAllocator, texture);
        }

        public void uploadPixels(Texture texture, ByteBuffer pixels) {
            uploadPixels(texture, pixels, 0, 0, texture.texWidth, texture.texHeight);
        }

        public void uploadPixels(Texture texture, ByteBuffer pixels, int x, int y, int w, int h) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ImageUtils.uploadPixels(this.device, this.physicalDevice, this.memoryAllocator, this.commandPool,
                        this.queues, x, y, w, h, texture.imageSize, texture.format.vulkan,
                        texture, pixels, stack, true);
            }
        }

        public Texture createEmpty(int imageSize, int texWidth, int texHeight, TextureFormat format) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ImageData imageData = ImageUtils.createImage(this.device, this.physicalDevice, this.memoryAllocator,
                        imageSize, texWidth, texHeight, format.vulkan, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, stack);
                long imageView = ImageViewsManager.createImageView(this.device, imageData.buffer, format.vulkan);
                return new Texture(imageData, imageSize, texWidth, texHeight, format, imageView, this);
            }
        }

        public Texture createPixels(ByteBuffer pixels, int imageSize, int texWidth, int texHeight, TextureFormat format) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ImageData imageData = ImageUtils.createImage(this.device, this.physicalDevice, this.memoryAllocator,
                        imageSize, texWidth, texHeight, format.vulkan, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, stack);
                ImageUtils.uploadPixels(this.device, this.physicalDevice, this.memoryAllocator, this.commandPool,
                        this.queues, 0, 0, texWidth, texHeight, imageSize, format.vulkan, imageData, pixels,
                        stack, true);
                long imageView = ImageViewsManager.createImageView(this.device, imageData.buffer, format.vulkan);
                return new Texture(imageData, imageSize, texWidth, texHeight, format, imageView, this);
            }
        }

        public Texture loadFile(String path) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                if (path.startsWith("file:/")) {
                    path = path.substring(6);
                }

                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);
                IntBuffer pChannels = stack.mallocInt(1);
                int channels = STBImage.STBI_rgb_alpha;
                ByteBuffer pixels = STBImage.stbi_load(path, pWidth, pHeight, pChannels, channels);
                int imageSize = pWidth.get(0) * pHeight.get(0) * channels;

                if (pixels == null) {
                    throw new RuntimeException("Failed to load texture image! " + STBImage.stbi_failure_reason());
                }

                Texture texture = createPixels(pixels, imageSize, pWidth.get(0), pHeight.get(0), TextureFormat.RGBA);
                STBImage.stbi_image_free(pixels);

                return texture;
            }
        }
    }
}
