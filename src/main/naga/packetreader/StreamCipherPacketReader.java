/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
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
