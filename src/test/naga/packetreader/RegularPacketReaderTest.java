package naga.packetreader;
/**
 * @author Christoffer Lerno 
 * @version $Revision$ $Date$   $Author$
 */

import junit.framework.*;
import naga.packetreader.RegularPacketReader;

public class RegularPacketReaderTest extends TestCase
{
	RegularPacketReader m_regularPacketReader;

	public void testRegularPacketReader() throws Exception
	{
		m_regularPacketReader = new RegularPacketReader(3, true);
		m_regularPacketReader.getBuffer().put(new byte[] { 0, 0, 4 });
		assertEquals(null, m_regularPacketReader.getNextPacket());
		m_regularPacketReader.getBuffer().put("Foo!".getBytes());
		assertEquals("Foo!", new String(m_regularPacketReader.getNextPacket()));

		m_regularPacketReader = new RegularPacketReader(3, true);
		m_regularPacketReader.getBuffer().put(new byte[] { 0, 0, 0 });
		assertEquals("", new String(m_regularPacketReader.getNextPacket()));

	}
}