package io.github.lunbun.pulsar.component.pipeline;

import io.github.lunbun.pulsar.component.presentation.SwapChain;
import io.github.lunbun.pulsar.component.setup.LogicalDevice;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

public final class RenderPass {
    public final long renderPass;

    protected RenderPass(long renderPass) {
        this.renderPass = renderPass;
    }

    protected void destroy(LogicalDevice device) {
        VK10.vkDestroyRenderPass(device.device, this.renderPass, null);
    }

    public static final class Builder {
        public final SwapChain swapChain;
        public final LogicalDevice device;
        private final List<RenderPass> renderPasses;

        public Builder(LogicalDevice device, SwapChain swapChain) {
            this.device = device;
            this.swapChain = swapChain;
            this.renderPasses = new ObjectArrayList<>();
        }

        public RenderPass createRenderPass() {
            long handle = this.createVkRenderPass();
            RenderPass renderPass = new RenderPass(handle);
            this.renderPasses.add(renderPass);
            return renderPass;
        }

        public void destroy() {
            for (int i = this.renderPasses.size() - 1; i >= 0; --i) {
                this.renderPasses.get(i).destroy(this.device);
                this.renderPasses.remove(i);
            }
        }

        private long createVkRenderPass() {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.callocStack(1, stack);
                colorAttachment.format(this.swapChain.imageFormat);
                colorAttachment.samples(VK10.VK_SAMPLE_COUNT_1_BIT);
                colorAttachment.loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR);
                colorAttachment.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE);
                colorAttachment.stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
                colorAttachment.stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE);
                colorAttachment.initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED);
                colorAttachment.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

                VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.callocStack(1, stack);
                colorAttachmentRef.attachment(0);
                colorAttachmentRef.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

                VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
                subpass.pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS);
                subpass.colorAttachmentCount(1);
                subpass.pColorAttachments(colorAttachmentRef);

                VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
                renderPassInfo.sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
                renderPassInfo.pAttachments(colorAttachment);
                renderPassInfo.pSubpasses(subpass);

                VkSubpassDependency.Buffer dependencies = VkSubpassDependency.callocStack(1, stack);
                dependencies.srcSubpass(VK10.VK_SUBPASS_EXTERNAL);
                dependencies.dstSubpass(0);
                dependencies.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
                dependencies.srcAccessMask(0);
                dependencies.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
                dependencies.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
                renderPassInfo.pDependencies(dependencies);

                LongBuffer pRenderPass = stack.mallocLong(1);

                if (VK10.vkCreateRenderPass(this.device.device, renderPassInfo, null, pRenderPass) != VK10.VK_NULL_HANDLE) {
                    throw new RuntimeException("Failed to create render pass!");
                }

                return pRenderPass.get(0);
            }
        }
    }
}
