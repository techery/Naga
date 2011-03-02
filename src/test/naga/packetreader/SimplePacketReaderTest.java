package naga.packetreader;
/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */

import junit.framework.TestCase;
import naga.exception.ProtocolViolationException;

import java.io.IOException;
import java.io.InputStream;

public class SimplePacketReaderTest extends TestCase
{
    SimplePacketReader m_simplePacketReader;

    private void putData(byte[] data) throws ProtocolViolationException
    {
        m_simplePacketReader.prepareBuffer();
        m_simplePacketReader.getBuffer().put(data);
        m_simplePacketReader.readFinished();
    }

    public void testReadFinished() throws Exception
    {
        m_simplePacketReader = new SimplePacketReader() {
            public byte[] read(InputStream stream) throws IOException
            {
                byte[] bytes = new byte[2];
                stream.mark(1);
                int read = stream.read(bytes);
                if (read != 2)
                {
                    stream.reset();
                    return null;
                }
                return bytes;
            }
        };
        putData("ABCDEFG".getBytes());
        assertEquals("AB", new String(m_simplePacketReader.getNextPacket()));
        assertEquals("CD", new String(m_simplePacketReader.getNextPacket()));
        assertEquals("EF", new String(m_simplePacketReader.getNextPacket()));
        assertEquals(null, m_simplePacketReader.getNextPacket());
        putData("123".getBytes());
        assertEquals("G1", new String(m_simplePacketReader.getNextPacket()));
        assertEquals("23", new String(m_simplePacketReader.getNextPacket()));
        assertEquals(null, m_simplePacketReader.getNextPacket());
    }
}