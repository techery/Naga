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
