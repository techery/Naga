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
			new DelimiterPacketReader((byte)0, 0, 1);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Min buffer must at least be 1 byte.", e.getMessage());
		}
		try
		{
			new DelimiterPacketReader((byte)0, 1, 0);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Read buffer cannot be be larger than the max packet size.", e.getMessage());
		}
		m_delimiterPacketReader = new DelimiterPacketReader((byte)0, 1, 20);
		assertEquals(20, m_delimiterPacketReader.getMaxPacketSize());
		m_delimiterPacketReader.setMaxPacketSize(19);
		assertEquals(19, m_delimiterPacketReader.getMaxPacketSize());
	}
}