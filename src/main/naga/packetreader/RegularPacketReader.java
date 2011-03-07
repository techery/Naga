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
    private final int m_headerSize;

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
        m_headerSize = headerSize;
	}

    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        if (byteBuffer.remaining() < m_headerSize) return null;
        byteBuffer.mark();
        int length = NIOUtils.getPacketSizeFromByteBuffer(byteBuffer, m_headerSize, m_bigEndian);
        if (byteBuffer.remaining() >= length)
        {
            byte[] packet = new byte[length];
            byteBuffer.get(packet);
            return packet;
        }
        else
        {
            byteBuffer.reset();
            return null;
        }
    }

}
