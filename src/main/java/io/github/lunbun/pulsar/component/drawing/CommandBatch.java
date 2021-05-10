package io.github.lunbun.pulsar.component.drawing;

import io.github.lunbun.pulsar.component.presentation.SwapChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public final class CommandBatch implements AutoCloseable {
    public final MemoryStack stack;

    public VkCommandBufferBeginInfo beginInfo;
    public VkRenderPassBeginInfo renderPassInfo;

    protected CommandBatch(SwapChain swapChain) {
        this.stack = MemoryStack.stackPush();

        this.createVulkanBatch(swapChain);
    }

    private void createVulkanBatch(SwapChain swapChain) {
        this.beginInfo = VkCommandBufferBeginInfo.callocStack(this.stack);
        this.beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        this.renderPassInfo = VkRenderPassBeginInfo.callocStack(this.stack);
        this.renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

        VkRect2D renderArea = VkRect2D.callocStack(this.stack);
        renderArea.offset(VkOffset2D.callocStack(this.stack).set(0, 0));
        renderArea.extent(swapChain.extent);
    }

    @Override
    public void close() {
        this.stack.pop();
    }

    public static final class Builder {
        private final SwapChain swapChain;

        public Builder(SwapChain swapChain) {
            this.swapChain = swapChain;
        }

        public CommandBatch createBatch() {
            return new CommandBatch(this.swapChain);
        }
    }
}
