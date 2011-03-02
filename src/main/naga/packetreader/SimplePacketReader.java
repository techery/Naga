package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public abstract class SimplePacketReader implements PacketReader
{
    public final static int MAX_BUFFER_SIZE = 64 * 1024;
    private final static ThreadLocal<ByteBuffer> SHARED_BUFFER = new ThreadLocal<ByteBuffer>()
    {
        @Override
        protected ByteBuffer initialValue()
        {
            return ByteBuffer.allocate(MAX_BUFFER_SIZE);
        }
    };

    private final ByteBufferInputStream m_stream;
    private byte[] m_remainder;

    public SimplePacketReader()
    {
        m_stream = new ByteBufferInputStream();
        m_remainder = null;
    }

    public void prepareBuffer() throws ProtocolViolationException
    {
        SHARED_BUFFER.get().clear();
        if (m_remainder != null)
        {
            SHARED_BUFFER.get().put(m_remainder);
            m_remainder = null;
        }
    }

    public ByteBuffer getBuffer()
    {
        return SHARED_BUFFER.get();
    }

    public abstract byte[] read(InputStream stream) throws IOException;

    public void readFinished() throws ProtocolViolationException
    {
        ByteBuffer buffer = SHARED_BUFFER.get();
        buffer.flip();
        m_stream.setBuffer(buffer);
    }

    public byte[] getNextPacket() throws ProtocolViolationException
    {
        try
        {
            byte[] packet = read(m_stream);
            if (packet == null)
            {
                ByteBuffer buffer = m_stream.m_buffer;
                if (buffer.remaining() != 0)
                {
                    if (buffer.remaining() == MAX_BUFFER_SIZE) throw new ProtocolViolationException("Buffered packet exceeded " + MAX_BUFFER_SIZE + " bytes.");
                    m_remainder = new byte[buffer.remaining()];
                    buffer.get(m_remainder);
                }
            }
            return packet;
        }
        catch (IOException e)
        {
            throw new ProtocolViolationException("Failed to parse packet");
        }
    }

    private static class ByteBufferInputStream extends InputStream
    {
        private ByteBuffer m_buffer;
        private int m_mark;

        private ByteBufferInputStream()
        {
        }


        @Override
        public boolean markSupported()
        {
            return true;
        }

        @Override
        public void reset() throws IOException
        {
            m_buffer.position(m_mark);
        }

        @Override
        public long skip(long l) throws IOException
        {
            int skipLength = Math.min((int)l, m_buffer.remaining());
            int newPosition = m_buffer.position() + skipLength;
            m_buffer.position(newPosition);
            return skipLength;
        }

        @Override
        public void mark(int i)
        {
            m_mark = m_buffer.position();
        }

        public void setBuffer(ByteBuffer buffer)
        {
            m_buffer = buffer;
        }

        @Override
        public int available() throws IOException
        {
            return m_buffer.remaining();
        }

        @Override
        public int read(byte[] bytes) throws IOException
        {
            return read(bytes, 0, bytes.length);
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException
        {
            int lengthToRead = Math.min(Math.min(bytes.length - offset, length), m_buffer.remaining());
            if (lengthToRead == 0) return 0;
            m_buffer.get(bytes, offset, lengthToRead);
            return lengthToRead;
        }

        public int read() throws IOException
        {
            return m_buffer.remaining() == 0 ? -1 : m_buffer.get();
        }
    }
}


