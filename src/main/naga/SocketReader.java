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

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A Socket reader handles read/writes on a socket.
 *
 * @author Christoffer Lerno
 */
class SocketReader
{
    private final NIOService m_nioService;
    private ByteBuffer m_previousBytes;
    private long m_bytesRead;

    SocketReader(NIOService nioService)
    {
        m_nioService = nioService;
        m_bytesRead = 0;
    }

    public int read(SocketChannel channel) throws IOException
    {
        // Retrieve the shared buffer.
        ByteBuffer buffer = getBuffer();

        // Clear the buffer.
        buffer.clear();

        // Create an offset if there are unconsumed bytes.
        if (m_previousBytes != null)
        {
            buffer.position(m_previousBytes.remaining());
        }

        // Read data
        int read = channel.read(buffer);

        // We might encounter the end of the socket stream here.
        if (read < 0) throw new EOFException("Buffer read -1");

        // If we have no space left in the buffer, we need to throw an exception.
        if (!buffer.hasRemaining()) throw new BufferOverflowException();

        // Increase the bytes read.
        m_bytesRead += read;

        // If nothing was read, simply return 0.
        if (read == 0) return 0;

        // If we read data, we need to insert the previous bytes.
        // We could avoid this at the cost of making the "read" method more complex in PacketReader.
        if (m_previousBytes != null)
        {
            // Remember the old position.
            int position = buffer.position();

            // Shift to position 0
            buffer.position(0);

            // Add the bytes.
            buffer.put(m_previousBytes);

            // Restore the position.
            buffer.position(position);

            // Clear the bytes we kept.
            m_previousBytes = null;
        }

        // Flip the buffer to prepare for reading.
        buffer.flip();

        return read;
    }

    /**
     * Moves any unread bytes to a buffer to be available later.
     */
    public void compact()
    {
        // Retrieve our shared buffer.
        ByteBuffer buffer = getBuffer();

        // If there is data remaining, copy that data.
        if (buffer.remaining() > 0)
        {
            m_previousBytes = NIOUtils.copy(buffer);
        }
    }

    /**
     * Return the number of raw bytes read.
     *
     * @return the number of bytes read.
     */
    public long getBytesRead()
    {
        return m_bytesRead;
    }

    /**
     * Returns the shared buffer (associated with the NIOService) for read/write.
     *
     * @return the shared buffer-
     */
    public ByteBuffer getBuffer()
    {
        return m_nioService.getSharedBuffer();
    }
}
