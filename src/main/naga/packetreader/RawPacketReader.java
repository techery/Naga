package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * This packet reader reads as many bytes as possible from the stream
 * and then bundles those bytes into a packet.
 *
 * @author Christoffer Lerno
 */
public class RawPacketReader implements PacketReader
{

    public final static RawPacketReader INSTANCE = new RawPacketReader();

    private RawPacketReader()
    {
    }

    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        byte[] packet = new byte[byteBuffer.remaining()];
        byteBuffer.get(packet);
        return packet;
    }

}
