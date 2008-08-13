package naga.packetreader;

import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * Class to read a byte stream delimited by a byte marking the end of a packet.
 * <p>
 * The delimiter will never appear in the packet itself.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class DelimiterPacketReader implements PacketReader
{
	private ByteBuffer m_currentBuffer;
	private byte[] m_buffer;
	private byte m_delimiter;

	/**
	 * Create a new reader with the default buffer size.
	 *
	 * @param delimiter the byte delimiter to use.
	 */
	public DelimiterPacketReader(byte delimiter)
	{
		this(80, delimiter);
	}

	/**
	 * Create a new reader with the given buffer size, delimited
	 * by the given byte.
	 *
	 * @param bufferSize the buffer size to use for the underlying ByteBuffer.
	 * @param delimiter the byte value of the delimiter.
	 */
	public DelimiterPacketReader(int bufferSize, byte delimiter)
	{
		m_currentBuffer = ByteBuffer.allocate(bufferSize);
		m_buffer = null;
		m_delimiter = delimiter;
	}

	public ByteBuffer getBuffer()
	{
		return m_currentBuffer;
	}

	public byte[] getNextPacket()
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