package io.github.lunbun.pulsar.component.pipeline;

public class GraphicsPipeline {
    public long pipelineLayout;
    public long pipeline;
    public final RenderPass renderPass;

    protected GraphicsPipeline(RenderPass renderPass) {
        this.renderPass = renderPass;
    }
}
