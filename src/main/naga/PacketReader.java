package naga;

import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * Interface for packet reader plugins to assist a socket in reading.
 * <p>
 * PacketReaders are in general intended to help splitting
 *
 * @author Christoffer Lerno
 */
public interface PacketReader
{

    // Send SKIP_PACKET to cause the returning byte to be discarded, while not stopping the read loop.
    public static byte[] SKIP_PACKET = new byte[0];

    /**
     * Create a new packet using the ByteBuffer given.
     * <p/>
     * If there isn't sufficient data to construct a packet, return null.
     *
     * @param byteBuffer the byte buffer to use.
     * @return the new packet created, or null if no packet could be created. The method will continously
     * be called until nextPacket returns null.
     * @throws ProtocolViolationException is there was an error constructing the packet.
     */
    byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException;

}
