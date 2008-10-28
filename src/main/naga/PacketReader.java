package naga;

import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * Interface for packet reader plugins to assist a socket in reading.
 * <p>
 * To implement a packet reader, the reader has to offer the currently
 * used byte buffer whenever the NIO service calls <code>PacketReader#getBuffer()</code>
 * <p>
 * <code>PacketReader#getNextPacket()</code> should return a byte-array if it is possible to create
 * one from the data loaded into the buffer(s).
 * <p>
 * <em>Note that getNextPacket() will be called repeatedly until it returns null.</em>
 *
 * @author Christoffer Lerno
 */
public interface PacketReader
{
	/**
	 * Return the currently used byte buffer. The NIOSocket will use this byte buffer and perform
	 * a SocketChannel.read(ByteBuffer) on it.
	 * <p>
	 * The reader is guaranteed not to have this method called more than once before a
	 * call to <code>PacketReader#getNextPacket()</code> is made.
	 *
	 * @return the byte buffer to use.
	 * @throws ProtocolViolationException if a protocol violation was detected when
	 * reading preparing the buffer.
	 */
	ByteBuffer getBuffer() throws ProtocolViolationException;

	/**
	 * Return the next packet constructed from the data read in the buffers.
	 * <p>
	 * This call may or may not have been proceeded by a call to getBuffer().
	 * <p>
	 * The calling thread will call this method repeatedly until it returns null.
	 *
	 * @return a byte array containing the data of a packet, or null if not packet can be created
	 * yet from the data read.
	 * @throws ProtocolViolationException if a protocol violation was detected when
	 * parsing the next packet.
	 */
	byte[] getNextPacket() throws ProtocolViolationException;
}
