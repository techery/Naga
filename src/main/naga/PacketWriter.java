package naga;

import java.nio.ByteBuffer;

/**
 * Interface for classes implementing packet writing strategies.
 * <p>
 * The method {@link naga.PacketWriter#setPacket(byte[])} initializes for writer for output of a new
 * packet. Implementing classes can assume that setPacket is never called unless
 * {@link #isEmpty()}
 * returns true. The {@link #getBuffer()}
 * method should return a {@link java.nio.ByteBuffer} containing
 * the next chunk of data to output on the socket.
 * <p>
 * The implementation is similar to:
 * <p>
 * <code>
 * while (!packetWriter.isEmpty()) channel.write(packetWriter.getBuffer());
 * </code>
 * <p>
 * In other words, it is ok to split a single packet into several byte buffers each are handed in
 * turn for every call to {@link #getBuffer()}. (See {@link naga.packetwriter.RegularPacketWriter} source code for an example.)
 *
 * @author Christoffer Lerno
 */
public interface PacketWriter
{
	/**
	 * Set the next packet to write.
	 * 
	 * @param bytes an array of bytes representing the next packet.
	 */
	void setPacket(byte[] bytes);

	/**
	 * Determines if the packet writer has more data to write.
	 * <p>
	 * Classes will never invoke {@link #setPacket(byte[])} unless
	 * <code>isEmpty</code> returns true.
	 *
	 * @return true if everything buffered in the writer is writen, false otherwise.
	 */
	boolean isEmpty();

	/**
	 * The current byte buffer to write to the socket.
	 * <p>
	 * Note that the socket does no rewinding or similar of the buffer,
	 * the only way it interacts with the buffer is by calling
	 * {@link java.nio.channels.SocketChannel#write(ByteBuffer)}, so the implementing
	 * class needs to make sure that the buffer is in the right state.
	 * <p>
	 * This code will not be called unless {@link #isEmpty()} returns false.
	 *
	 * @return the byte buffer to send data from to the socket.
	 */
	ByteBuffer getBuffer();
}
