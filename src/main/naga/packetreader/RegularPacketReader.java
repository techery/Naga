package naga.packetreader;

import naga.NIOUtils;
import naga.PacketReader;
import naga.exception.ProtocolViolationException;

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
 */
public class RegularPacketReader implements PacketReader
{
	private final boolean m_bigEndian;
	private ByteBuffer m_header;
	private ByteBuffer m_content;
	private int m_contentSize = -1;
    private byte[] m_nextPacket;

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
        m_nextPacket = null;
	}

    /**
     * Prepares the buffer
     *
     * Makes sure that the content buffer is initialized if the header has finished reading.
	 *
	 * @throws ProtocolViolationException if the header indicates that the size of the
	 * content is equal to or larger than Integer.MAX_VALUE.
     */
    public void prepareBuffer() throws ProtocolViolationException
    {
    }

    /**
	 * Return the next buffer to use.
	 *
	 * @return the next buffer to use.
	 * larger or equal to Integer.MAX_VALUE.
	 */
	public ByteBuffer getBuffer()
	{
        return m_contentSize < 0 ? m_header : m_content;
	}

    public void readFinished() throws ProtocolViolationException
    {
        if (m_contentSize >= 0)
        {
            if (m_content.remaining() > 0) return;
            m_nextPacket = m_content.array();
            m_content = null;
            m_header.rewind();
            m_contentSize = -1;
            return;
        }
        if (m_header.remaining() > 0) return;
        m_contentSize = NIOUtils.getPacketSizeFromByteBuffer(m_header, m_bigEndian);
        if (m_contentSize < 0 || m_contentSize >= Integer.MAX_VALUE)
        {
            throw new ProtocolViolationException("Content size out of range, was: " + m_contentSize);
        }
        if (m_contentSize == 0)
        {
            m_header.rewind();
            m_nextPacket = new byte[0];
            m_contentSize = -1;
            return;
        }
        m_content = ByteBuffer.allocate(m_contentSize);
    }

    /**
	 * Return the next packet or null if no complete packet can be constructed.
	 *
	 * @return the next packet available or null if none is available.
	 */
	public byte[] getNextPacket() throws ProtocolViolationException
	{
        byte[] packet = m_nextPacket;
        m_nextPacket = null;
		return packet;
	}
}
