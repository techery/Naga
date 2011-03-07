package naga.packetreader;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class RegularPacketReaderTest extends TestCase
{
	RegularPacketReader m_regularPacketReader;

	public void testRegularPacketReader() throws Exception
	{
		m_regularPacketReader = new RegularPacketReader(3, true);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] { 0, 0, 3 });
        assertEquals(null, m_regularPacketReader.nextPacket(byteBuffer));
        assertEquals(3, byteBuffer.remaining());
        byteBuffer = ByteBuffer.wrap(new byte[] { 0, 0, 3, 65, 66, 67, 68});
        assertEquals("ABC", new String(m_regularPacketReader.nextPacket(byteBuffer)));
        assertEquals(1, byteBuffer.remaining());
		m_regularPacketReader = new RegularPacketReader(3, true);
        byteBuffer = ByteBuffer.wrap(new byte[] { 0, 0, 0 });
        assertEquals("", new String(m_regularPacketReader.nextPacket(byteBuffer)));

	}
}