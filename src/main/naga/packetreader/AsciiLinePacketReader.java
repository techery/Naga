package naga.packetreader;

import naga.PacketReader;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class AsciiLinePacketReader implements PacketReader
{
	private ByteBuffer m_currentBuffer;
	private String m_currentString;

	public AsciiLinePacketReader()
	{
		this(80);
	}

	public AsciiLinePacketReader(int bufferSize)
	{
		m_currentBuffer = ByteBuffer.allocate(bufferSize);
		m_currentString = "";
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
			byte[] bytes = new byte[m_currentBuffer.remaining()];
			m_currentBuffer.get(bytes);
			m_currentString += new String(bytes);
			m_currentBuffer.clear();
		}
		int firstLineBreak = m_currentString.indexOf('\n');
		if (firstLineBreak == -1) return null;

		String substring = m_currentString.substring(0, firstLineBreak);
		if (firstLineBreak < m_currentString.length() - 1)
		{
			m_currentString = m_currentString.substring(firstLineBreak + 1);
		}
		else
		{
			m_currentString = "";
		}
		return substring.getBytes();
	}
}
