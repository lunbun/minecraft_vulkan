package io.github.lunbun.pulsar.component.vertex;

import io.github.lunbun.pulsar.component.drawing.CommandBatch;
import io.github.lunbun.pulsar.component.drawing.CommandPool;
import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import io.github.lunbun.pulsar.component.setup.PhysicalDevice;
import io.github.lunbun.pulsar.component.setup.QueueManager;
import io.github.lunbun.pulsar.struct.vertex.BufferData;
import io.github.lunbun.pulsar.util.vulkan.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public final class VertexBuffer {
    public final BufferData vertex;
    public final int count;

    private final Builder builder;

    protected VertexBuffer(Builder builder, BufferData vertex, int count) {
        this.builder = builder;
        this.vertex = vertex;
        this.count = count;
    }

    public void destroy() {
        this.builder.destroy(this);
    }

    public static final class Builder {
        private final LogicalDevice device;
        private final PhysicalDevice physicalDevice;
        private final CommandPool commandPool;
        private final CommandBatch.Builder commandBatches;
        private final QueueManager queues;
        private final MemoryAllocator allocator;

        public Builder(LogicalDevice device, PhysicalDevice physicalDevice, CommandPool commandPool, CommandBatch.Builder commandBatches, QueueManager queues, MemoryAllocator allocator) {
            this.device = device;
            this.physicalDevice = physicalDevice;
            this.commandPool = commandPool;
            this.commandBatches = commandBatches;
            this.queues = queues;
            this.allocator = allocator;
        }

        protected void destroy(VertexBuffer vertexBuffer) {
            BufferUtils.destroy(this.device, this.allocator, vertexBuffer.vertex);
        }

        public VertexBuffer createVertexBuffer(int count, long size) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                BufferData vertex = BufferUtils.createBuffer(this.device, this.physicalDevice, this.allocator, size,
                        VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, stack);

                return new VertexBuffer(this, vertex, count);
            }
        }

        public void uploadData(VertexBuffer buffer, Vertex[] vertices) {
            BufferUtils.uploadData(this.device, this.physicalDevice, this.allocator, this.commandPool,
                    this.commandBatches, this.queues, buffer.vertex, byteBuffer -> {
                        for (Vertex vertex : vertices) {
                            vertex.write(byteBuffer);
                        }
                    });
        }
    }
}
