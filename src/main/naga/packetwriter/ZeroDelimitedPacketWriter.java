package naga.packetwriter;

/**
 * Writes a bytestream delimited by 0.
 *
 * @author Christoffer Lerno
 */
public class ZeroDelimitedPacketWriter extends DelimiterPacketWriter
{
    public ZeroDelimitedPacketWriter()
    {
        super((byte) 0);
    }
}
