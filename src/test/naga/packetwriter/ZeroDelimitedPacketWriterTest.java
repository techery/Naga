package naga.packetwriter;
/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */

import junit.framework.TestCase;

import java.nio.ByteBuffer;

public class ZeroDelimitedPacketWriterTest extends TestCase
{
    public void testZeroDelimitedPacketWriter() throws Exception
    {
        ZeroDelimitedPacketWriter writer = new ZeroDelimitedPacketWriter();
        ByteBuffer part1 = ByteBuffer.wrap("FOO".getBytes());
        ByteBuffer part2 = ByteBuffer.wrap("bar".getBytes());
        ByteBuffer[] result = writer.write(new ByteBuffer[]{part1, part2});
        ByteBuffer buffer = ByteBuffer.allocate(100);
        for (ByteBuffer b : result)
        {
            buffer.put(b);
        }
        buffer.flip();

        byte[] resultByte = new byte[buffer.limit()];
        buffer.get(resultByte);
        assertEquals("FOObar\0", new String(resultByte));
        ByteBuffer part3 = ByteBuffer.wrap("BAZ".getBytes());
        ByteBuffer part4 = ByteBuffer.wrap("fooo".getBytes());
        ByteBuffer[] result2 = writer.write(new ByteBuffer[]{part3, part4});
        ByteBuffer buffer2 = ByteBuffer.allocate(100);
        for (ByteBuffer b : result2)
        {
            buffer2.put(b);
        }
        buffer2.flip();

        byte[] resultByte2 = new byte[buffer2.limit()];
        buffer2.get(resultByte2);
        assertEquals("BAZfooo\0", new String(resultByte2));
    }
}