package naga.packetreader;

import naga.NIOUtils;
import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * Reads packet of the format
 * <p>
 * <code>
 * [header 1-4 bytes] => content size
 * <br>
 * [content] => 0-255/0-65535/0-16777215/0-2147483646
 * </code>
 * <p>
 * Note that the maximum size for 4 bytes is a signed 32 bit int, not unsigned.
 * 
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class RegularPacketReader implements PacketReader
{
	private final boolean m_bigEndian;
	private ByteBuffer m_header;
	private ByteBuffer m_content;
	private int m_contentSize = -1;

	/**
	 * Creates a regular packet reader with the given header size.
	 *
	 * @param headerSize the header size, 1 - 4 bytes.
	 * @param bigEndian big endian (largest byte first) or little endian (smallest byte first)
	 */
	public RegularPacketReader(int headerSize, boolean bigEndian)
	{
		if (headerSize < 1 || headerSize > 4) throw new IllegalArgumentException("Header must be between 1 and 4 bytes long.");
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
			if (m_contentSize < 0 || m_contentSize >= Integer.MAX_VALUE)
			{
				throw new IllegalArgumentException("Content size out of range, was: " + m_contentSize);
			}
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
