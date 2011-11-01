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
package naga;

import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * Interface for packet reader plugins to assist a socket in reading.
 * <p>
 * PacketReaders are in general intended to help splitting
 *
 * @author Christoffer Lerno
 */
public interface PacketReader
{

    // Send SKIP_PACKET to cause the returning byte to be discarded, while not stopping the read loop.
    public static byte[] SKIP_PACKET = new byte[0];

    /**
     * Create a new packet using the ByteBuffer given.
     * <p/>
     * If there isn't sufficient data to construct a packet, return null.
     *
     * @param byteBuffer the byte buffer to use.
     * @return the new packet created, or null if no packet could be created. The method will continously
     * be called until nextPacket returns null.
     * @throws ProtocolViolationException is there was an error constructing the packet.
     */
    byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException;

}
