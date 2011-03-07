package naga.packetreader;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import naga.exception.ProtocolViolationException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AsciiLinePacketReaderTest extends TestCase
{
	AsciiLinePacketReader m_asciiLinePacketReader;

	public void testAsciiLinePacketReader() throws Exception
	{
		m_asciiLinePacketReader = new AsciiLinePacketReader();
		byte[] notALine = "Foo".getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(notALine);
        assertEquals(3, byteBuffer.remaining());
        assertEquals(null, m_asciiLinePacketReader.nextPacket(byteBuffer));
        assertEquals(3, byteBuffer.remaining());
        byteBuffer = ByteBuffer.wrap("Foo\n".getBytes());
        assertEquals("Foo", new String(m_asciiLinePacketReader.nextPacket(byteBuffer)));
        assertEquals(0, byteBuffer.remaining());
        byteBuffer = ByteBuffer.wrap("Foo\n2".getBytes());
        assertEquals("Foo", new String(m_asciiLinePacketReader.nextPacket(byteBuffer)));
        assertEquals(1, byteBuffer.remaining());
        byteBuffer = ByteBuffer.wrap("\n\nFoo\n\nBar\nFoobar".getBytes());
		assertEquals(0, m_asciiLinePacketReader.nextPacket(byteBuffer).length);
        assertEquals(0, m_asciiLinePacketReader.nextPacket(byteBuffer).length);
        assertEquals("Foo", new String(m_asciiLinePacketReader.nextPacket(byteBuffer)));
        assertEquals(0, m_asciiLinePacketReader.nextPacket(byteBuffer).length);
        assertEquals("Bar", new String(m_asciiLinePacketReader.nextPacket(byteBuffer)));
        assertEquals(null, m_asciiLinePacketReader.nextPacket(byteBuffer));
	}

	public void testOverflow() throws ProtocolViolationException
	{
		m_asciiLinePacketReader = new AsciiLinePacketReader(3);
        try
        {
            m_asciiLinePacketReader.nextPacket(ByteBuffer.wrap("Foo!\n".getBytes()));
            fail("Should throw error");
        }
        catch (IOException e)
        {

        }
	}

}