package io.github.lunbun.pulsar.struct.vertex;

public final class BufferData {
    public final long buffer;
    public final int memoryType;
    public final long memory;
    public final int pointer;
    public final long size;
    public final int allocSize;

    public BufferData(long buffer, int memoryType, long memory, int pointer, long size, int allocSize) {
        this.buffer = buffer;
        this.memoryType = memoryType;
        this.memory = memory;
        this.pointer = pointer;
        this.size = size;
        this.allocSize = allocSize;
    }
}
