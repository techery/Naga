package naga.packetreader;

import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RawPacketReader implements PacketReader
{
	private final ByteBuffer m_buffer;

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
		byte[] packet = new byte[m_buffer.remaining()];
		m_buffer.get(packet);
		m_buffer.clear();
		return packet;
	}
}
