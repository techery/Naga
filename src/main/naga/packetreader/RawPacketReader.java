package naga.packetreader;

import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * This packet reader reads bytes one at a time from the stream, creating 1 byte packets.
 *  
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RawPacketReader implements PacketReader
{
	private final ByteBuffer m_buffer;

	/**
	 * Create a new reader instance.
	 */
	public RawPacketReader()
	{
		m_buffer = ByteBuffer.allocate(1);
	}

	public ByteBuffer getBuffer()
	{
		return m_buffer;
	}

	public byte[] getNextPacket()
	{
		if (m_buffer.hasRemaining()) return null;
		m_buffer.flip();
		byte[] packet = new byte[1];
		m_buffer.get(packet);
		m_buffer.clear();
		return packet;
	}
}
