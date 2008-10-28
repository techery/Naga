package naga.packetwriter;

import naga.NIOUtils;
import naga.PacketWriter;

import java.nio.ByteBuffer;

/**
 * Writes packet of the format
 * <p>
 * <code>
 * [header 1-4 bytes] => content size
 * <br>
 * [content] => 0-255/0-65535/0-16777215/0-2147483646
 * </code>
 * <p>
 * Note that the maximum size for 4 bytes is a signed 32 bit int, not unsigned.
 * <p>
 * The packet writer will not validate outgoing packets, so make sure that
 * the packet content size will fit in the header. I.e. make sure that if you have
 * a 1 byte header, you do not send packets larger than 255 bytes, if two bytes, larger than 65535 and
 * so on.
 *
 * @author Christoffer Lerno
 */
public class RegularPacketWriter implements PacketWriter
{
	private ByteBuffer m_header;
	private ByteBuffer m_content;
	private final boolean m_bigEndian;
	private final int m_headerSize;

	/**
	 * Creates a regular packet writer with the given header size.
	 *
	 * @param headerSize the header size, 1 - 4 bytes.
	 * @param bigEndian big endian (largest byte first) or little endian (smallest byte first)
	 */
	public RegularPacketWriter(int headerSize, boolean bigEndian)
	{
		if (headerSize < 1 || headerSize > 4) throw new IllegalArgumentException("Header must be between 1 and 4 bytes long.");
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
