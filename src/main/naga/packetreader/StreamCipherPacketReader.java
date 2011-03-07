package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public class StreamCipherPacketReader implements PacketReader
{
    private final Cipher m_cipher;
    private ByteBuffer m_internalBuffer;
    private PacketReader m_reader;

    public StreamCipherPacketReader(Cipher cipher, PacketReader reader)
    {
        m_cipher = cipher;
        m_reader = reader;
    }

    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        if (m_internalBuffer == null)
        {
            m_internalBuffer = ByteBuffer.allocate(m_cipher.getOutputSize(byteBuffer.remaining()));
        }
        else
        {
            ByteBuffer newBuffer = ByteBuffer.allocate(m_cipher.getOutputSize(byteBuffer.remaining()) + m_internalBuffer.remaining());
            newBuffer.put(m_internalBuffer);
            m_internalBuffer = newBuffer;
        }
        try
        {
            int consumed = m_cipher.update(byteBuffer, m_internalBuffer);
        }
        catch (ShortBufferException e)
        {
            throw new ProtocolViolationException("Short buffer");
        }
        m_internalBuffer.flip();
        byte[] packet = m_reader.nextPacket(m_internalBuffer);
        if (m_internalBuffer.remaining() == 0) m_internalBuffer = null;
        return packet;
    }

}
