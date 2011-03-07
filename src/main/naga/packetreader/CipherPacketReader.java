package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.nio.ByteBuffer;

/**
 * Example filter reader that decrypts the stream before passing it to its underlying reader.
 *
 * @author Christoffer Lerno
 */
public class CipherPacketReader implements PacketReader
{
    private final Cipher m_cipher;
    private ByteBuffer m_internalBuffer;
    private PacketReader m_reader;

    /**
     * Creates a new CipherPacketReader.
     *
     * @param cipher the cipher to use.
     * @param reader the underlying packet reader we wish to employ.
     */
    public CipherPacketReader(Cipher cipher, PacketReader reader)
    {
        m_cipher = cipher;
        m_reader = reader;
    }

    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        if (m_internalBuffer == null)
        {
            // No buffer, so simply allocate sufficient memory.
            m_internalBuffer = ByteBuffer.allocate(m_cipher.getOutputSize(byteBuffer.remaining()));
        }
        else
        {
            // Only create a new buffer if there is new incoming data
            if (byteBuffer.remaining() > 0)
            {
                // Allocate enough memory to hold the new and the already decrypted data.
                ByteBuffer newBuffer = ByteBuffer.allocate(m_cipher.getOutputSize(byteBuffer.remaining()) + m_internalBuffer.remaining());

                // Move the decrypted data to front.
                newBuffer.put(m_internalBuffer);

                // Update the internal buffer.
                m_internalBuffer = newBuffer;

            }
        }
        // Decrypt the new data-
        if (byteBuffer.remaining() > 0)
        {
            try
            {
                m_cipher.update(byteBuffer, m_internalBuffer);
            }
            catch (ShortBufferException e)
            {
                throw new ProtocolViolationException("Short buffer");
            }
            // Prepare the data
            m_internalBuffer.flip();
        }

        byte[] packet = m_reader.nextPacket(m_internalBuffer);
        if (m_internalBuffer.remaining() == 0) m_internalBuffer = null;
        return packet;
    }

}
