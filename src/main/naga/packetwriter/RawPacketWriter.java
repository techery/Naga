package naga.packetwriter;

import naga.PacketWriter;

import java.nio.ByteBuffer;

/**
 * Writes a byte packet to the stream without doing to it at all.
 * <p>
 * This is commonly used when one wants to output text or similarly
 * delimited data.
 *
 * @author Christoffer Lerno
 */
public class RawPacketWriter implements PacketWriter
{
	private ByteBuffer m_buffer;

	/**
	 * Creates a new writer.
	 */
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
