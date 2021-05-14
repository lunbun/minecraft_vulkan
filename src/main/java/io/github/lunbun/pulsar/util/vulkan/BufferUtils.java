package io.github.lunbun.pulsar.util.vulkan;

import io.github.lunbun.pulsar.component.drawing.CommandBatch;
import io.github.lunbun.pulsar.component.drawing.CommandBuffer;
import io.github.lunbun.pulsar.component.drawing.CommandPool;
import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import io.github.lunbun.pulsar.component.setup.PhysicalDevice;
import io.github.lunbun.pulsar.component.setup.QueueManager;
import io.github.lunbun.pulsar.component.vertex.MemoryAllocator;
import io.github.lunbun.pulsar.struct.setup.QueueFamily;
import io.github.lunbun.pulsar.struct.vertex.BufferData;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;

public final class BufferUtils {
    private BufferUtils() { }

    public static int findMemoryType(PhysicalDevice physicalDevice, int typeFilter, int properties) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.callocStack(stack);
            VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice.device, memProperties);

            for (int i = 0; i < memProperties.memoryTypeCount(); ++i) {
                if (((typeFilter & (1 << i)) != 0) &&
                        ((memProperties.memoryTypes(i).propertyFlags() & properties) == properties)) {
                    return i;
                }
            }

            throw new RuntimeException("Failed to find suitable memory type!");
        }
    }

    public static VkMemoryRequirements getMemoryRequirements(LogicalDevice device, long buffer, MemoryStack stack) {
        VkMemoryRequirements memRequirements = VkMemoryRequirements.callocStack(stack);
        VK10.vkGetBufferMemoryRequirements(device.device, buffer, memRequirements);
        return memRequirements;
    }

    public static long createBuffer(LogicalDevice device, long size, int usage, MemoryStack stack) {
        VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
        bufferInfo.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
        bufferInfo.size(size);
        bufferInfo.usage(usage);
        bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

        LongBuffer pVertexBuffer = stack.mallocLong(1);

        if (VK10.vkCreateBuffer(device.device, bufferInfo, null, pVertexBuffer) != VK10.VK_SUCCESS) {
            throw new RuntimeException("Failed to create vertex buffer!");
        }

        return pVertexBuffer.get(0);
    }

    public static BufferData createBuffer(LogicalDevice device, PhysicalDevice physicalDevice, MemoryAllocator allocator, long size, int usage, int properties, MemoryStack stack) {
        long buffer = BufferUtils.createBuffer(device, size, usage, stack);

        VkMemoryRequirements memoryRequirements = getMemoryRequirements(device, buffer, stack);
        int memoryType = findMemoryType(physicalDevice, memoryRequirements.memoryTypeBits(), properties);
        long memory = allocator.heap(memoryType);
        int pointer = allocator.malloc(memoryType, (int) memoryRequirements.size());

        VK10.vkBindBufferMemory(device.device, buffer, memory, pointer);
        return new BufferData(buffer, memoryType, memory, pointer, size, (int) memoryRequirements.size());
    }

    public static void copyBuffer(CommandPool commandPool, CommandBatch.Builder commandBatches, QueueManager queues, long src, long dst, int size) {
        CommandBuffer buffer = commandPool.allocateBuffer();
        try (CommandBatch batch = commandBatches.createBatch()) {
            buffer.startRecording(batch, VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            buffer.copyBuffer(src, dst, size, batch);
            buffer.endRecording();

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(batch.stack);
            submitInfo.sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(batch.stack.pointers(buffer.buffer));

            VK10.vkQueueSubmit(queues.getQueue(QueueFamily.GRAPHICS), submitInfo, VK10.VK_NULL_HANDLE);
            VK10.vkQueueWaitIdle(queues.getQueue(QueueFamily.GRAPHICS));

            commandPool.freeBuffer(buffer);
        }
    }

    public static void destroy(LogicalDevice device, MemoryAllocator allocator, BufferData data) {
        VK10.vkDestroyBuffer(device.device, data.buffer, null);
        allocator.free(data.memoryType, data.pointer, data.allocSize);
    }

    public static void uploadData(LogicalDevice device, PhysicalDevice physicalDevice, MemoryAllocator allocator,
                                  CommandPool commandPool, CommandBatch.Builder commandBatches, QueueManager queues,
                                  BufferData buffer, Consumer<ByteBuffer> bufferWriter) {
        // TODO: should we be creating a staging buffer every time we upload, or should we store it?
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pData = stack.mallocPointer(1);
            BufferData staging = BufferUtils.createBuffer(device, physicalDevice, allocator,
                    buffer.size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stack);
            VK10.vkMapMemory(device.device, staging.memory, staging.pointer, staging.size, 0, pData);
            {
                bufferWriter.accept(pData.getByteBuffer(0, (int) staging.size));
            }
            VK10.vkUnmapMemory(device.device, staging.memory);
            BufferUtils.copyBuffer(commandPool, commandBatches, queues, staging.buffer, buffer.buffer,
                    (int) buffer.size);
            destroy(device, allocator, staging);
        }
    }
}
