package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * This packet reader reads as many bytes as possible from the stream
 * and then bundles those bytes into a packet.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RawPacketReader implements PacketReader
{
	public final static int DEFAULT_BUFFER_SIZE = 256;
	private final ByteBuffer m_buffer;

	/**
	 * Create a new reader instance. With a given read buffer size (this is
	 * how many bytes the packet reader will max read in a single pass).
	 *
	 * @param bufferSize the buffer size to use.
	 */
	public RawPacketReader(int bufferSize)
	{
		m_buffer = ByteBuffer.allocate(bufferSize);
	}

	/**
	 * Create a new reader instance with the default buffer size.
	 */
	public RawPacketReader()
	{
		this(DEFAULT_BUFFER_SIZE);
	}

	public ByteBuffer getBuffer()
	{
		return m_buffer;
	}

	public byte[] getNextPacket() throws ProtocolViolationException
	{
		if (m_buffer.position() == 0) return null;
		m_buffer.flip();
		byte[] packet = new byte[m_buffer.remaining()];
		m_buffer.get(packet);
		m_buffer.clear();
		return packet;
	}
}
