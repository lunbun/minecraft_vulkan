package io.github.lunbun.pulsar.util.shader;

import org.lwjgl.system.NativeResource;
import org.lwjgl.util.shaderc.Shaderc;

import java.nio.ByteBuffer;

// https://github.com/Naitsirc98/Vulkan-Tutorial-Java/blob/ff0567a6635322d0413196f2ceffe338eef52bdb/src/main/java/javavulkantutorial/ShaderSPIRVUtils.java#L68
public final class SPIRV implements NativeResource {
    private final long handle;
    private ByteBuffer bytecode;

    public SPIRV(long handle, ByteBuffer bytecode) {
        this.handle = handle;
        this.bytecode = bytecode;
    }

    public ByteBuffer getBytecode() {
        return bytecode;
    }

    @Override
    public void free() {
        Shaderc.shaderc_result_release(handle);
        bytecode = null; // Help the GC
    }
}
