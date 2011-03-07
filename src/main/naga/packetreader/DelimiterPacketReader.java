package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * Class to read a byte stream delimited by a byte marking the end of a packet.
 * <p>
 * Since packets read with delimiters may potentially grow unbounded, you can also supply
 * a maximum buffer size to prevent an attacker from causing an out of memory
 * by continously sending data without the delimiter.
 * <p>
 * The delimiter will never appear in the packet itself.
 *
 * @author Christoffer Lerno
 */
public class DelimiterPacketReader implements PacketReader
{
	private volatile int m_maxPacketSize;
	private byte m_delimiter;

	/**
	 * Create a new reader with the default min buffer size and unlimited max buffer size.
	 *
	 * @param delimiter the byte delimiter to use.
	 */
	public DelimiterPacketReader(byte delimiter)
	{
		this(delimiter, -1);
	}

	/**
	 * Create a new reader with the given min and max buffer size
	 * delimited by the given byte.
	 *
	 * @param delimiter the byte value of the delimiter.
	 * @param maxPacketSize the maximum number of bytes read before throwing an
	 * IOException. -1 means the packet has no size limit.
	 * @throws IllegalArgumentException if maxPacketSize < 1
	 */
	public DelimiterPacketReader(byte delimiter, int maxPacketSize)
	{
		if (maxPacketSize < 1 && maxPacketSize != -1)
		{
			throw new IllegalArgumentException("Max packet size must be larger that 1, was: " + maxPacketSize);
		}
		m_delimiter = delimiter;
		m_maxPacketSize = maxPacketSize;
	}

	/**
	 * Get the current maximum buffer size.
	 *
	 * @return the current maximum size.
	 */
	public int getMaxPacketSize()
	{
		return m_maxPacketSize;
	}

	/**
	 * Set the new maximum packet size.
	 * <p>
	 * This method is thread-safe, but will not
	 * affect reads in progress.
	 *
	 * @param maxPacketSize the new maximum packet size.
	 */
	public void setMaxPacketSize(int maxPacketSize)
	{
		m_maxPacketSize = maxPacketSize;
	}

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        byteBuffer.mark();
        int bytesRead = 0;
        while (byteBuffer.remaining() > 0)
        {
            int ch = byteBuffer.get();
            if (ch == m_delimiter)
            {
                byte[] packet = new byte[bytesRead];
                byteBuffer.reset();
                byteBuffer.get(packet);
                byteBuffer.get();
                return packet;
            }
            bytesRead++;
            if (m_maxPacketSize > 0 && bytesRead > m_maxPacketSize) throw new ProtocolViolationException("Packet exceeds max " + m_maxPacketSize);
        }
        byteBuffer.reset();
        return null;
    }

}