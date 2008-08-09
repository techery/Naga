package naga.packetwriter;

import naga.PacketWriter;
import naga.NIOUtils;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RegularPacketWriter implements PacketWriter
{
	private ByteBuffer m_header;
	private ByteBuffer m_content;
	private final boolean m_bigEndian;
	private final int m_headerSize;

	public RegularPacketWriter(int headerSize, boolean bigEndian)
	{
		if (headerSize < 1 || headerSize > 3) throw new IllegalArgumentException("Header must be between 1 and 3 bytes long.");
		m_bigEndian = bigEndian;
		m_headerSize = headerSize;
		m_header = ByteBuffer.allocate(0);
		m_content = ByteBuffer.allocate(0);
	}

	public ByteBuffer getBuffer()
	{
		return m_header.hasRemaining() ? m_header : m_content;
	}

	public boolean isEmpty()
	{
		return !m_header.hasRemaining() && !m_content.hasRemaining();
	}

	public void setPacket(byte[] bytes)
	{
		if (!isEmpty()) throw new IllegalStateException("Attempted to add new packet before the previous was sent.");
		m_header = NIOUtils.getByteBufferFromPacketSize(m_headerSize, bytes.length, m_bigEndian);
		m_content = ByteBuffer.wrap(bytes);
	}
}
