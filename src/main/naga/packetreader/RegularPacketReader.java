package naga.packetreader;

import naga.NIOUtils;
import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RegularPacketReader implements PacketReader
{
	private final boolean m_bigEndian;
	private ByteBuffer m_header;
	private ByteBuffer m_content;
	private int m_contentSize = -1;

	public RegularPacketReader(int headerSize, boolean bigEndian)
	{
		if (headerSize < 1 || headerSize > 3) throw new IllegalArgumentException("Header must be between 1 and 3 bytes long.");
		m_bigEndian = bigEndian;
		m_header = ByteBuffer.allocate(headerSize);
		m_contentSize = -1;
		m_content = null;
	}

	public ByteBuffer getBuffer()
	{
		if (m_header.hasRemaining()) return m_header;
		prepareContentBuffer();
		return m_content;
	}

	private void prepareContentBuffer()
	{
		if (m_contentSize < 0 && !m_header.hasRemaining())
		{
			m_contentSize = NIOUtils.getPacketSizeFromByteBuffer(m_header, m_bigEndian);
			m_content = ByteBuffer.allocate(m_contentSize);
		}
	}

	public byte[] getNextPacket()
	{
		prepareContentBuffer();
		if (m_contentSize < 0 || m_content.hasRemaining())
		{
			return null;
		}
		byte[] content = m_content.array();
		m_content = null;
		m_header.rewind();
		m_contentSize = -1;
		return content;
	}
}
