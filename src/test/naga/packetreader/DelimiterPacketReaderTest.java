package naga.packetreader;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;

public class DelimiterPacketReaderTest extends TestCase
{
	DelimiterPacketReader m_delimiterPacketReader;

	public void testDelimiterPacketReader() throws Exception
	{
		try
		{
			new DelimiterPacketReader((byte)0, 0);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Max packet size must be larger that 1, was: 0", e.getMessage());
		}
		m_delimiterPacketReader = new DelimiterPacketReader((byte)0, 20);
		assertEquals(20, m_delimiterPacketReader.getMaxPacketSize());
		m_delimiterPacketReader.setMaxPacketSize(19);
		assertEquals(19, m_delimiterPacketReader.getMaxPacketSize());
	}
}