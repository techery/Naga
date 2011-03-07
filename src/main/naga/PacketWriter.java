package naga;

import java.nio.ByteBuffer;

/**
 * Interface for classes implementing packet writing strategies.
 * <p>
 * The method {@link naga.PacketWriter#write(ByteBuffer[])} converts an incoming byte array
 * to an outgoing byte array.
 * 
 * @author Christoffer Lerno
 */
public interface PacketWriter
{
    /**
     * Convert the incoming bytes to the bytes to be serialized.
     *
     * @param byteBuffer an array of ByteBuffers containing data the bytes to be written.
     * @return the resulting array of ByteBuffers.
     */
    ByteBuffer[] write(ByteBuffer[] byteBuffer);
}
