package naga.packetwriter;

import naga.PacketWriter;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;

/**
 * Example Writer that encrypts the outgoing stream using a Cipher object.
 *
 * @author Christoffer Lerno
 */
public class CipherPacketWriter implements PacketWriter
{
    private final Cipher m_cipher;
    private PacketWriter m_packetWriter;

    public CipherPacketWriter(Cipher cipher, PacketWriter packetWriter)
    {
        m_cipher = cipher;
        m_packetWriter = packetWriter;
    }

    public PacketWriter getPacketWriter()
    {
        return m_packetWriter;
    }

    public void setPacketWriter(PacketWriter packetWriter)
    {
        m_packetWriter = packetWriter;
    }

    public ByteBuffer[] write(ByteBuffer[] byteBuffer)
    {
        byteBuffer = m_packetWriter.write(byteBuffer);
        ByteBuffer[] resultBuffer = new ByteBuffer[byteBuffer.length];
        try
        {
            for (int i = 0; i < byteBuffer.length; i++)
            {
                resultBuffer[i] = ByteBuffer.allocate(m_cipher.getOutputSize(byteBuffer[i].remaining()));
                if (i == byteBuffer.length - 1)
                {
                    m_cipher.doFinal(byteBuffer[i], resultBuffer[i]);
                }
                else
                {
                    m_cipher.update(byteBuffer[i], resultBuffer[i]);
                }
                assert byteBuffer[i].remaining() == 0;
                resultBuffer[i].flip();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return resultBuffer;
    }
}
