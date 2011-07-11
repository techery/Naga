package naga.packetwriter;

/**
 * Writes a bytestream delimited by '\n'.
 *
 * @author Christoffer Lerno
 */
public class AsciiLinePacketWriter extends DelimiterPacketWriter
{
    public AsciiLinePacketWriter()
    {
        super((byte)'\n');
    }
}
