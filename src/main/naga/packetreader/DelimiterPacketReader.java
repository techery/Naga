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
 * @version $Revision$ $Date$   $Author$
 */
public class DelimiterPacketReader implements PacketReader
{
	public final static int DEFAULT_READ_BUFFER_SIZE = 256;
	private ByteBuffer m_currentBuffer;
	private volatile int m_maxPacketSize;
	private byte[] m_buffer;
	private byte m_delimiter;

	/**
	 * Create a new reader with the default min buffer size and unlimited max buffer size.
	 *
	 * @param delimiter the byte delimiter to use.
	 */
	public DelimiterPacketReader(byte delimiter)
	{
		this(delimiter, DEFAULT_READ_BUFFER_SIZE, -1);
	}

	/**
	 * Create a new reader with the given min and max buffer size
	 * delimited by the given byte.
	 *
	 * @param delimiter the byte value of the delimiter.
	 * @param readBufferSize the size of the read buffer (i.e. how many
	 * bytes are read in a single pass) - this only has effect on read
	 * efficiency and memory requirements.
	 * @param maxPacketSize the maximum number of bytes read before throwing a
	 * ProtocolException. -1 means the packet has no size limit.
	 * @throws IllegalArgumentException if maxPacketSize < readBufferSize or if
	 * readBufferSize < 1.
	 */
	public DelimiterPacketReader(byte delimiter, int readBufferSize, int maxPacketSize)
	{
		if (readBufferSize < 1) throw new IllegalArgumentException("Min buffer must at least be 1 byte.");
		if (maxPacketSize > -1 && readBufferSize > maxPacketSize)
		{
			throw new IllegalArgumentException("Read buffer size be larger than max packet size.");
		}
		m_currentBuffer = ByteBuffer.allocate(readBufferSize);
		m_buffer = null;
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

	/**
	 * Return the currently used byte buffer.
	 *
	 * @return the byte buffer to use.
	 * @throws ProtocolViolationException if the internal buffer already exceeds the maximum size.
	 */
	public ByteBuffer getBuffer() throws ProtocolViolationException
	{
		if (m_maxPacketSize > -1 && m_buffer != null && m_buffer.length > m_maxPacketSize)
		{
			throw new ProtocolViolationException("Packet size exceeds " + m_maxPacketSize);
		}
		return m_currentBuffer;
	}

	public byte[] getNextPacket() throws ProtocolViolationException
	{
		if (m_currentBuffer.position() > 0)
		{
			m_currentBuffer.flip();
			int oldBufferLength = m_buffer == null ? 0 : m_buffer.length;
			byte[] newBuffer = new byte[oldBufferLength + m_currentBuffer.remaining()];
			if (m_buffer != null)
			{
				System.arraycopy(m_buffer, 0, newBuffer, 0, m_buffer.length);
			}
			m_currentBuffer.get(newBuffer, oldBufferLength, m_currentBuffer.remaining());
			m_currentBuffer.clear();
			m_buffer = newBuffer;
		}
		if (m_buffer == null) return null;
		for (int i = 0; i < m_buffer.length; i++)
		{
			if (m_buffer[i] == m_delimiter)
			{
				byte[] packet = new byte[i];
				System.arraycopy(m_buffer, 0, packet, 0, i);
				if (i > m_buffer.length - 2)
				{
					m_buffer = null;
				}
				else
				{
					byte[] newBuffer = new byte[m_buffer.length - i - 1];
					System.arraycopy(m_buffer, i + 1, newBuffer, 0, newBuffer.length);
					m_buffer = newBuffer;
				}
				return packet;
			}
		}
		return null;
	}
}