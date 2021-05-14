package io.github.lunbun.pulsar.component.drawing;

import io.github.lunbun.pulsar.component.presentation.SwapChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public final class CommandBatch implements AutoCloseable {
    public final MemoryStack stack;

    private final SwapChain swapChain;
    private VkCommandBufferBeginInfo beginInfo;
    private VkRenderPassBeginInfo renderPassInfo;
    private VkBufferCopy bufferCopyRegion;

    protected CommandBatch(SwapChain swapChain) {
        this.stack = MemoryStack.stackPush();
        this.swapChain = swapChain;
    }

    public VkCommandBufferBeginInfo getBeginInfo() {
        if (this.beginInfo == null) {
            this.beginInfo = VkCommandBufferBeginInfo.callocStack(this.stack);
            this.beginInfo.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
        }

        return this.beginInfo;
    }

    public VkRenderPassBeginInfo getRenderPassInfo() {
        if (this.renderPassInfo == null) {
            this.renderPassInfo = VkRenderPassBeginInfo.callocStack(this.stack);
            this.renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

            VkRect2D renderArea = VkRect2D.callocStack(this.stack);
            renderArea.offset(VkOffset2D.callocStack(this.stack).set(0, 0));
            renderArea.extent(this.swapChain.extent);
            this.renderPassInfo.renderArea(renderArea);
        }

        return this.renderPassInfo;
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
