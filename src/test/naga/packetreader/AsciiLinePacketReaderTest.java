package naga.packetreader;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import naga.exception.ProtocolViolationException;

public class AsciiLinePacketReaderTest extends TestCase
{
	AsciiLinePacketReader m_asciiLinePacketReader;

	public void testAsciiLinePacketReader() throws Exception
	{
		m_asciiLinePacketReader = new AsciiLinePacketReader();
		assertEquals(DelimiterPacketReader.DEFAULT_READ_BUFFER_SIZE, m_asciiLinePacketReader.getBuffer().remaining());
		byte[] notALine = "Foo".getBytes();
		m_asciiLinePacketReader.getBuffer().put(notALine);
		assertEquals(null, m_asciiLinePacketReader.getNextPacket());
		byte[] endOfLine = "\n".getBytes();
		m_asciiLinePacketReader.getBuffer().put(endOfLine);
		assertEquals("Foo", new String(m_asciiLinePacketReader.getNextPacket()));
		assertEquals(DelimiterPacketReader.DEFAULT_READ_BUFFER_SIZE, m_asciiLinePacketReader.getBuffer().remaining());
		byte[] multiLines = "\n\nFoo\n\nBar\nFoobar".getBytes();
		m_asciiLinePacketReader.getBuffer().put(multiLines);
		assertEquals(0, m_asciiLinePacketReader.getNextPacket().length);
		assertEquals(0, m_asciiLinePacketReader.getNextPacket().length);
		assertEquals("Foo", new String(m_asciiLinePacketReader.getNextPacket()));
		assertEquals(0, m_asciiLinePacketReader.getNextPacket().length);
		assertEquals("Bar", new String(m_asciiLinePacketReader.getNextPacket()));
		assertEquals(null, m_asciiLinePacketReader.getNextPacket());
	}

	public void testOverflow() throws ProtocolViolationException
	{
		m_asciiLinePacketReader = new AsciiLinePacketReader(3, 3);
		assertEquals(3, m_asciiLinePacketReader.getBuffer().remaining());
		byte[] notALine = "Foo".getBytes();
		m_asciiLinePacketReader.getBuffer().put(notALine);
		assertEquals(null, m_asciiLinePacketReader.getNextPacket());
		m_asciiLinePacketReader.getBuffer().put("!".getBytes());
		assertEquals(null, m_asciiLinePacketReader.getNextPacket());
		byte[] endOfLine = "\n".getBytes();
		try
		{
			m_asciiLinePacketReader.getBuffer().put(endOfLine);
			fail("Should fail");
		}
		catch (ProtocolViolationException e)
		{}
	}

}