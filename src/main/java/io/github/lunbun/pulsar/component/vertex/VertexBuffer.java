package io.github.lunbun.pulsar.component.vertex;

import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import io.github.lunbun.pulsar.component.setup.PhysicalDevice;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Map;

public final class VertexBuffer {
    public final long buffer;
    public final long memory;
    public final long size;
    public final int count;

    private final Builder builder;
    private final long index;

    protected VertexBuffer(Builder builder, long index, long buffer, long memory, long size, int count) {
        this.builder = builder;
        this.index = index;

        this.buffer = buffer;
        this.memory = memory;
        this.size = size;
        this.count = count;
    }

    public void destroy() {
        this.builder.destroy(this.index);
    }

    public static final class Builder {
        private final LogicalDevice device;
        private final PhysicalDevice physicalDevice;
        private final Map<Long, VertexBuffer> buffers;
        private long index;

        public Builder(LogicalDevice device, PhysicalDevice physicalDevice) {
            this.device = device;
            this.physicalDevice = physicalDevice;
            this.buffers = new Long2ObjectOpenHashMap<>();
            this.index = 0;
        }

        protected void destroy(long index) {
            VertexBuffer buffer = this.buffers.get(index);
            VK10.vkDestroyBuffer(this.device.device, buffer.buffer, null);
            VK10.vkFreeMemory(this.device.device, buffer.memory, null);
            this.buffers.remove(index);
        }

        public void destroy() {
            for (Map.Entry<Long, VertexBuffer> buffer : this.buffers.entrySet()) {
                this.destroy(buffer.getKey());
            }
        }

        private int findMemoryType(int typeFilter, int properties) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.callocStack(stack);
                VK10.vkGetPhysicalDeviceMemoryProperties(this.physicalDevice.device, memProperties);

                for (int i = 0; i < memProperties.memoryTypeCount(); ++i) {
                    if (((typeFilter & (1 << i)) != 0) &&
                            ((memProperties.memoryTypes(i).propertyFlags() & properties) == properties)) {
                        return i;
                    }
                }

                throw new RuntimeException("Failed to find suitable memory type!");
            }
        }

        public VertexBuffer createVertexBuffer(int count, long size) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
                bufferInfo.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
                bufferInfo.size(size);
                bufferInfo.usage(VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
                bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

                LongBuffer pVertexBuffer = stack.mallocLong(1);

                if (VK10.vkCreateBuffer(this.device.device, bufferInfo, null, pVertexBuffer) != VK10.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create vertex buffer!");
                }

                long vertexBuffer = pVertexBuffer.get(0);

                VkMemoryRequirements memRequirements = VkMemoryRequirements.callocStack(stack);
                VK10.vkGetBufferMemoryRequirements(this.device.device, vertexBuffer, memRequirements);

                VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
                allocInfo.sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
                allocInfo.allocationSize(memRequirements.size());
                allocInfo.memoryTypeIndex(this.findMemoryType(memRequirements.memoryTypeBits(),
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));

                LongBuffer pVertexBufferMemory = stack.mallocLong(1);

                if (VK10.vkAllocateMemory(this.device.device, allocInfo, null, pVertexBufferMemory) != VK10.VK_SUCCESS) {
                    throw new RuntimeException("Failed to allocate vertex buffer memory!");
                }

                long vertexBufferMemory = pVertexBufferMemory.get(0);
                VK10.vkBindBufferMemory(this.device.device, vertexBuffer, vertexBufferMemory, 0);

                long index = ++this.index;
                if (this.index == -1) {
                    throw new RuntimeException("Ran out of buffer ids!");
                }

                VertexBuffer vbo = new VertexBuffer(this, index, vertexBuffer, vertexBufferMemory, bufferInfo.size(), count);
                this.buffers.put(index, vbo);
                return vbo;
            }
        }

        public void uploadData(VertexBuffer buffer, Vertex[] vertices) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pData = stack.mallocPointer(1);
                VK10.vkMapMemory(this.device.device, buffer.memory, 0, buffer.size, 0, pData);
                {
                    ByteBuffer byteBuffer = pData.getByteBuffer(0, (int) buffer.size);
                    for (Vertex vertex : vertices) {
                        vertex.write(byteBuffer);
                    }
                }
                VK10.vkUnmapMemory(this.device.device, buffer.memory);
            }
        }
    }
}
