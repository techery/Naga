package naga.packetreader;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import naga.exception.ProtocolViolationException;

public class RegularPacketReaderTest extends TestCase
{
	RegularPacketReader m_regularPacketReader;

    private void putData(byte[] data) throws ProtocolViolationException
    {
        m_regularPacketReader.prepareBuffer();
        m_regularPacketReader.getBuffer().put(data);
        m_regularPacketReader.readFinished();
    }
	public void testRegularPacketReader() throws Exception
	{
		m_regularPacketReader = new RegularPacketReader(3, true);
        putData(new byte[] { 0, 0, 4 });
		assertEquals(null, m_regularPacketReader.getNextPacket());
        putData("Foo!".getBytes());
		assertEquals("Foo!", new String(m_regularPacketReader.getNextPacket()));

		m_regularPacketReader = new RegularPacketReader(3, true);
        putData(new byte[] { 0, 0, 0 });
		assertEquals("", new String(m_regularPacketReader.getNextPacket()));

	}
}