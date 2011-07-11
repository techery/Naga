package naga.packetwriter;

import naga.NIOUtils;
import naga.PacketWriter;

import java.nio.ByteBuffer;

/**
 * Class to write a byte stream delimited by a byte marking the end of a packet.
 *
 * @author Christoffer Lerno
 */
public class DelimiterPacketWriter implements PacketWriter
{
    private ByteBuffer m_endByte;

    public DelimiterPacketWriter(byte endByte)
    {
        m_endByte = ByteBuffer.wrap(new byte[] { endByte });
    }

    public ByteBuffer[] write(ByteBuffer[] byteBuffer)
    {
        m_endByte.rewind();
        return NIOUtils.concat(byteBuffer, m_endByte);
    }
}
