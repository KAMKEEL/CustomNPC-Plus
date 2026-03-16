package io.netty.buffer;

/**
 * TEMPORARY STUB -- auto-generated for compilation.
 * Netty ByteBuf stub for core compilation.
 */
public abstract class ByteBuf {
    public abstract ByteBuf writeInt(int value);
    public abstract int readInt();
    public abstract ByteBuf writeByte(int value);
    public abstract byte readByte();
    public abstract ByteBuf writeShort(int value);
    public abstract short readShort();
    public abstract ByteBuf writeLong(long value);
    public abstract long readLong();
    public abstract ByteBuf writeFloat(float value);
    public abstract float readFloat();
    public abstract ByteBuf writeDouble(double value);
    public abstract double readDouble();
    public abstract ByteBuf writeBoolean(boolean value);
    public abstract boolean readBoolean();
    public abstract ByteBuf writeBytes(byte[] src);
    public abstract ByteBuf readBytes(byte[] dst);
    public abstract int readableBytes();
    public abstract boolean isReadable();
}
