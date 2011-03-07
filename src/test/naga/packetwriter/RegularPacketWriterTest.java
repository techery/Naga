package naga.packetwriter;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class RegularPacketWriterTest extends TestCase
{
	RegularPacketWriter m_regularPacketWriter;

	public void testRegularPacketWriter() throws Exception
	{
		m_regularPacketWriter = new RegularPacketWriter(3, true);
        ByteBuffer[] result = m_regularPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("Foo!".getBytes()) });
        assertEquals("[0, 0, 4]", Arrays.toString(result[0].array()));
        assertEquals("[70, 111, 111, 33]", Arrays.toString(result[1].array()));
        assertEquals(2, result.length);
        assertEquals("[0, 0, 0]", Arrays.toString(m_regularPacketWriter.write(new ByteBuffer[] { ByteBuffer.allocate(0) })[0].array()));
	}

    // See Bug 5
    public void testFourByteHeader() throws Exception
    {
        m_regularPacketWriter = new RegularPacketWriter(4, true);
        ByteBuffer[] result =  m_regularPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("Foo!".getBytes()) });
        assertEquals("[0, 0, 0, 4]", Arrays.toString(result[0].array()));
        assertEquals("[70, 111, 111, 33]", Arrays.toString(result[1].array()));
        assertEquals(2, result.length);
        assertEquals("[0, 0, 0, 0]", Arrays.toString(m_regularPacketWriter.write(new ByteBuffer[] { ByteBuffer.allocate(0) })[0].array()));
    }
}