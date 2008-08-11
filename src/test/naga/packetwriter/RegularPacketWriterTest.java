package naga.packetwriter;
/**
 * @author Christoffer Lerno 
 * @version $Revision$ $Date$   $Author$
 */

import junit.framework.TestCase;

public class RegularPacketWriterTest extends TestCase
{
	RegularPacketWriter m_regularPacketWriter;

	public void testRegularPacketWriter() throws Exception
	{
		m_regularPacketWriter = new RegularPacketWriter(3, true);
		m_regularPacketWriter.setPacket("Foo!".getBytes());
		assertEquals(false, m_regularPacketWriter.isEmpty());
		assertEquals(0, m_regularPacketWriter.getBuffer().get());
		assertEquals(0, m_regularPacketWriter.getBuffer().get());
		assertEquals(4, m_regularPacketWriter.getBuffer().get());
		byte[] buffer = new byte[4];
		m_regularPacketWriter.getBuffer().get(buffer);
		assertEquals(true, m_regularPacketWriter.isEmpty());
		assertEquals("Foo!", new String(buffer));

		m_regularPacketWriter = new RegularPacketWriter(3, true);
		m_regularPacketWriter.setPacket(new byte[0]);
		assertEquals(false, m_regularPacketWriter.isEmpty());
		assertEquals(0, m_regularPacketWriter.getBuffer().get());
		assertEquals(0, m_regularPacketWriter.getBuffer().get());
		assertEquals(0, m_regularPacketWriter.getBuffer().get());
		assertEquals(true, m_regularPacketWriter.isEmpty());
		assertEquals(false, m_regularPacketWriter.getBuffer().hasRemaining());
	}
}