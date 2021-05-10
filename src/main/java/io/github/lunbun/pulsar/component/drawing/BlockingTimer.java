package io.github.lunbun.pulsar.component.drawing;

import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;
import java.util.List;

public final class BlockingTimer {
    public final long handle;
    public final Type type;

    private final LogicalDevice device;

    protected BlockingTimer(LogicalDevice device, long handle, Type type) {
        this.device = device;

        this.handle = handle;
        this.type = type;
    }

    protected void destroy() {
        if (Type.SEMAPHORE.equals(this.type)) {
            VK10.vkDestroySemaphore(this.device.device, this.handle, null);
        } else if (Type.FENCE.equals(this.type)) {
            VK10.vkDestroyFence(this.device.device, this.handle, null);
        }
    }

    public enum Type {
        SEMAPHORE,
        FENCE
    }

    public static final class Builder {
        private final List<BlockingTimer> timers = new ObjectArrayList<>();
        private final LogicalDevice device;

        public Builder(LogicalDevice device) {
            this.device = device;
        }

        public BlockingTimer createTiming(Type type) {
            if (Type.SEMAPHORE.equals(type)) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
                    semaphoreInfo.sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

                    LongBuffer pSemaphore = stack.longs(VK10.VK_NULL_HANDLE);
                    if (VK10.vkCreateSemaphore(this.device.device, semaphoreInfo, null, pSemaphore) != VK10.VK_SUCCESS) {
                        throw new RuntimeException("Failed to create semaphores!");
                    }

                    BlockingTimer timer = new BlockingTimer(this.device, pSemaphore.get(0), type);
                    this.timers.add(timer);
                    return timer;
                }
            } else if (Type.FENCE.equals(type)) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
                    fenceInfo.sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                    fenceInfo.flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);

                    LongBuffer pFence = stack.longs(VK10.VK_NULL_HANDLE);
                    if (VK10.vkCreateFence(this.device.device, fenceInfo, null, pFence) != VK10.VK_SUCCESS) {
                        throw new RuntimeException("Failed to create fence!");
                    }

                    BlockingTimer timer = new BlockingTimer(this.device, pFence.get(0), type);
                    this.timers.add(timer);
                    return timer;
                }
            } else {
                return null;
            }
        }

        public void destroy() {
            for (BlockingTimer timer : this.timers) {
                timer.destroy();
            }
        }
    }
}
