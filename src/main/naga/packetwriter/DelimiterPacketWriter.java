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
