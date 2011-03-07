package naga;

import naga.packetwriter.RawPacketWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A helper class to handle writes on a socket.
 *
 * @author Christoffer Lerno
 */
class SocketWriter
{
    private long m_bytesWritten;
    private ByteBuffer[] m_writeBuffers;
    private PacketWriter m_packetWriter;
    private Object m_tag;
    private int m_currentBuffer;

    SocketWriter()
    {
        m_bytesWritten = 0;
        m_writeBuffers = null;
        m_packetWriter = RawPacketWriter.INSTANCE;
    }

    public PacketWriter getPacketWriter()
    {
        return m_packetWriter;
    }

    public void setPacketWriter(PacketWriter packetWriter)
    {
        m_packetWriter = packetWriter;
    }

    public boolean isEmpty()
    {
        return m_writeBuffers == null;
    }

    public void setPacket(byte[] data, Object tag)
    {
        if (!isEmpty()) throw new IllegalStateException("This method should only called when m_writeBuffers == null");

        // Set the current packet
        m_writeBuffers = m_packetWriter.write(new ByteBuffer[] { ByteBuffer.wrap(data) });
        m_currentBuffer = 0;
        m_tag = tag;
    }

    public boolean write(SocketChannel channel) throws IOException
    {
        // If the packet is empty, just clear data and return true
        if (m_writeBuffers == null
            || (m_currentBuffer == m_writeBuffers.length - 1
                && !m_writeBuffers[m_currentBuffer].hasRemaining()))
        {
            m_writeBuffers = null;
            return true;
        }

        // Write as much as possible to the channel.
        long written = channel.write(m_writeBuffers, m_currentBuffer, m_writeBuffers.length - m_currentBuffer);

        // If nothing is written, then the buffer is full and writing should end temporarily.
        if (written == 0) return false;

        // Add the number of bytes written.
        m_bytesWritten += written;

        // Delete written buffers, update currentBuffer
        for (int i = m_currentBuffer; i < m_writeBuffers.length; i++)
        {
            if (m_writeBuffers[i].hasRemaining())
            {
                m_currentBuffer = i;
                break;
            }
            m_writeBuffers[i] = null;
        }

        // If the current buffer is empty, clear all.
        if (m_writeBuffers[m_currentBuffer] == null)
        {
            m_writeBuffers = null;
        }
        return true;
    }

    public long getBytesWritten()
    {
        return m_bytesWritten;
    }

    public Object getTag()
    {
        return m_tag;
    }
}
