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
