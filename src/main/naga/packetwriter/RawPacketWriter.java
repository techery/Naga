package naga.packetwriter;

import naga.PacketWriter;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RawPacketWriter implements PacketWriter
{
	private ByteBuffer m_buffer;

	public RawPacketWriter()
	{
		m_buffer = null;
	}

	public void setPacket(byte[] bytes)
	{
		m_buffer = ByteBuffer.wrap(bytes);
	}

	public ByteBuffer getBuffer()
	{
		return m_buffer;
	}

	public boolean isEmpty()
	{
		return m_buffer == null || !m_buffer.hasRemaining();
	}
}
