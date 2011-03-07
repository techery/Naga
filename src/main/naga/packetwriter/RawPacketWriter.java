package naga.packetwriter;

import naga.PacketWriter;

import java.nio.ByteBuffer;

/**
 * Writes a byte packet to the stream without doing any changes to it.
 * <p>
 * This is the commonly case when one wants to output text or similarly
 * delimited data.
 *
 * @author Christoffer Lerno
 */
public class RawPacketWriter implements PacketWriter
{
    public static RawPacketWriter INSTANCE = new RawPacketWriter();
    
    private RawPacketWriter()
    {
    }

    public ByteBuffer[] write(ByteBuffer[] byteBuffers)
    {
        return byteBuffers;
    }
}
