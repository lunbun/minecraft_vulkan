package io.github.lunbun.quasar.client.render;

import io.github.lunbun.pulsar.PulsarApplication;
import io.github.lunbun.pulsar.component.drawing.CommandBatch;
import io.github.lunbun.pulsar.component.drawing.CommandBuffer;
import io.github.lunbun.pulsar.component.drawing.Framebuffer;
import io.github.lunbun.pulsar.component.pipeline.GraphicsPipeline;
import io.github.lunbun.pulsar.component.pipeline.RenderPass;
import io.github.lunbun.pulsar.component.pipeline.Shader;
import io.github.lunbun.pulsar.struct.setup.DeviceExtension;
import io.github.lunbun.pulsar.struct.setup.DeviceType;
import io.github.lunbun.pulsar.struct.setup.GraphicsCardPreference;
import io.github.lunbun.pulsar.struct.setup.QueueFamily;
import io.github.lunbun.quasar.client.engine.framework.glfw.GLFWWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class QuasarRenderer {
    private static final Logger LOGGER = LogManager.getLogger("Quasar");
    private static final PulsarApplication pulsar = new PulsarApplication("Minecraft");
    private static long window;

    public static void initWindow() {
        GLFWWindow.disableClientAPI();
        GLFWWindow.setResizable(true);
    }

    public static void createWindow(long handle) {
        window = handle;
        pulsar.setWindow(handle);
    }

    public static void resizeFramebuffer(int width, int height) {
        pulsar.framebufferResized();
    }

    private static void createRenderer() {
        Shader shader = new Shader("shader/shader.vert", "shader/shader.frag");
        RenderPass renderPass = pulsar.renderPasses.createRenderPass();
        GraphicsPipeline pipeline = pulsar.pipelines.createPipeline(shader, renderPass);
        pulsar.framebuffers.createFramebuffers(renderPass);

        pulsar.commandPool.allocateBuffers(pulsar.framebuffers.framebuffers.size());
        try (CommandBatch batch = pulsar.commandBatches.createBatch()) {
            for (int i = 0; i < pulsar.commandPool.buffers.size(); ++i) {
                CommandBuffer buffer = pulsar.commandPool.buffers.get(i);
                Framebuffer framebuffer = pulsar.framebuffers.framebuffers.get(i);

                buffer.startRecording(batch);
                buffer.startRenderPass(renderPass, framebuffer, batch);
                buffer.bindPipeline(pipeline);
                buffer.draw(3, 1, 0, 0);
                buffer.endRenderPass();
                buffer.endRecording();
            }
        }
    }

    public static void initVulkan() {
        LOGGER.info("Initializing Vulkan");
        GraphicsCardPreference preference = new GraphicsCardPreference(
                DeviceType.INTEGRATED,
                new QueueFamily[] { QueueFamily.GRAPHICS, QueueFamily.PRESENT },
                new DeviceExtension[] { DeviceExtension.SWAP_CHAIN }
        );
        pulsar.requestGraphicsCard(preference);

        pulsar.addRecreateHandler(ignored -> {
            createRenderer();
        });

        pulsar.initialize();

        createRenderer();

        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
            pulsar.frameRenderer.drawFrame(pulsar.commandPool.buffers);
        }
        pulsar.endLoop();
    }

    public static void cleanup() {
        pulsar.exit();
    }
}
