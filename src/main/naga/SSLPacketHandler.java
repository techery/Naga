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
import naga.packetreader.RawPacketReader;
import naga.packetwriter.RawPacketWriter;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public class SSLPacketHandler implements PacketReader, PacketWriter
{
    private final static Executor TASK_HANDLER = Executors.newSingleThreadExecutor();

    private final static ThreadLocal<ByteBuffer> SSL_BUFFER = new ThreadLocal<ByteBuffer>()
    {
        @Override
        protected ByteBuffer initialValue()
        {
            // Should be plenty.
            return ByteBuffer.allocate(64 * 1024);
        }
    };

    private final SSLEngine m_engine;
    private PacketReader m_reader;
    private PacketWriter m_writer;
    private ByteBuffer m_partialIncomingBuffer;
    private ByteBuffer[] m_initialOutBuffer;
    private final NIOSocket m_socket;
    private final SSLSocketChannelResponder m_responder;
    private boolean m_sslInitiated;

    public SSLPacketHandler(SSLEngine engine, NIOSocket socket, SSLSocketChannelResponder responder)
    {
        m_engine = engine;
        m_socket = socket;
        m_partialIncomingBuffer = null;
        m_writer = RawPacketWriter.INSTANCE;
        m_reader = RawPacketReader.INSTANCE;
        m_responder = responder;
        m_sslInitiated = false;
    }

    public PacketReader getReader()
    {
        return m_reader;
    }

    public void setReader(PacketReader reader)
    {
        m_reader = reader;
    }

    public PacketWriter getWriter()
    {
        return m_writer;
    }

    public void setWriter(PacketWriter writer)
    {
        m_writer = writer;
    }

    private void queueSSLTasks()
    {
        if (!m_sslInitiated) return;
        int tasksScheduled = 0;
        Runnable task;
        while ((task = m_engine.getDelegatedTask()) != null)
        {
            TASK_HANDLER.execute(task);
            tasksScheduled++;
        }
        if (tasksScheduled == 0)
        {
            return;
        }
        TASK_HANDLER.execute(new Runnable()
        {
            public void run()
            {
                m_socket.queue(new Runnable()
                {
                    public void run()
                    {
                        reactToHandshakeStatus(m_engine.getHandshakeStatus());
                    }
                });
            }
        });
    }

    public byte[] nextPacket(ByteBuffer byteBuffer) throws ProtocolViolationException
    {
        if (!m_sslInitiated)
        {
            return m_reader.nextPacket(byteBuffer);
        }

        try
        {
            // Retrieve the local buffer.
            ByteBuffer targetBuffer = SSL_BUFFER.get();
            targetBuffer.clear();

            // Unwrap the data (both buffers should be sufficiently large)
            SSLEngineResult result = m_engine.unwrap(byteBuffer, targetBuffer);
            switch (result.getStatus())
            {
                case BUFFER_UNDERFLOW:
                    // Right, let's wait for more data.
                    return null;
                case BUFFER_OVERFLOW:
                    // This should never happen as we are ensuring the buffer is large enough.
                    throw new ProtocolViolationException("SSL Buffer Overflow");
                case CLOSED:
                    m_responder.connectionBroken(m_socket, new EOFException("SSL Connection closed"));
                    return null;
                case OK:
                    // Do nothing, just follow the flow.
            }
            // We might need to queue tasks or send data as a response to this packet.
            reactToHandshakeStatus(result.getHandshakeStatus());

            return retrieveDecryptedPacket(targetBuffer);
        }
        catch (SSLException e)
        {
            m_responder.closeDueToSSLException(e);
            return null;
        }
    }

    private void reactToHandshakeStatus(SSLEngineResult.HandshakeStatus status)
    {
        if (!m_sslInitiated) return;
        switch (status)
        {
            case NOT_HANDSHAKING:
            case NEED_UNWRAP:
                break;
            case NEED_TASK:
                queueSSLTasks();
                break;
            case FINISHED:
                m_socket.write(new byte[0]);
                break;
            case NEED_WRAP:
                m_socket.write(new byte[0]);
                break;
        }
    }

    private byte[] retrieveDecryptedPacket(ByteBuffer targetBuffer) throws ProtocolViolationException
    {
        // Prepare the buffer for reading.
        targetBuffer.flip();

        // Join the buffer with the partial buffer, this is because we need to internally buffer data that has been decrypted but does not yet form a complete packet.
        m_partialIncomingBuffer = NIOUtils.join(m_partialIncomingBuffer, targetBuffer);

        // Skip if the data is empty. This will be the case during handshaking.
        if (m_partialIncomingBuffer == null || m_partialIncomingBuffer.remaining() == 0) return SKIP_PACKET;

        // Delegate packet creation to the reader.
        return m_reader.nextPacket(m_partialIncomingBuffer);
    }

    public ByteBuffer[] write(ByteBuffer[] byteBuffers)
    {
        if (!m_sslInitiated)
        {
            return m_writer.write(byteBuffers);
        }

        // Check if we are done handshaking.
        if (m_engine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
        {
            if (!NIOUtils.isEmpty(byteBuffers))
            {
                // If this is regular data, store this in the initial outbuffer.
                m_initialOutBuffer = NIOUtils.concat(m_initialOutBuffer, m_writer.write(byteBuffers));
                byteBuffers = new ByteBuffer[0];
            }
            // Borrow the shared buffer.
            ByteBuffer buffer = SSL_BUFFER.get();
            ByteBuffer[] buffers = null;
            try
            {
                // Create handshake data.
                SSLEngineResult result = null;
                while (m_engine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP)
                {
                    buffer.clear();
                    result = m_engine.wrap(byteBuffers, buffer);
                    buffer.flip();
                    buffers = NIOUtils.concat(buffers, NIOUtils.copy(buffer));
                }

                // If we for some reason entered here but did not need to wrap anything, exit.
                if (result == null) return null;

                if (result.getStatus() != SSLEngineResult.Status.OK) throw new SSLException("Unexpectedly not ok wrapping handshake data, was " + result.getStatus());

                reactToHandshakeStatus(result.getHandshakeStatus());
            }
            catch (SSLException e)
            {
                // Better error handling required!
                throw new RuntimeException(e);
            }
            return buffers;
        }

        // We are not handshaking, so encrypt the data using wrap

        // Use the shared buffer.
        ByteBuffer buffer = SSL_BUFFER.get();
        buffer.clear();

        if (NIOUtils.isEmpty(byteBuffers))
        {
            // Exit early if we have no data to encrypt.
            if (m_initialOutBuffer == null) return null;
        }
        else
        {
            // Only convert non-empty buffers
            byteBuffers = m_writer.write(byteBuffers);
        }

        // If we have an initial buffer, send it.
        if (m_initialOutBuffer != null)
        {
            byteBuffers = NIOUtils.concat(m_initialOutBuffer, byteBuffers);
            m_initialOutBuffer = null;
        }

        ByteBuffer[] encrypted = null;

        // While we have things left to encrypt.
        while (!NIOUtils.isEmpty(byteBuffers))
        {
            // Clear our huge buffer.
            buffer.clear();
            try
            {
                m_engine.wrap(byteBuffers, buffer);
            }
            catch (SSLException e)
            {
                throw new RuntimeException(e);
            }
            buffer.flip();

            // Copy the result.
            encrypted = NIOUtils.concat(encrypted, NIOUtils.copy(buffer));
        }

        // Return our encrypted data.
        return encrypted;
    }

    public SSLEngine getSSLEngine()
    {
        return m_engine;
    }

    void begin() throws SSLException
    {
        m_engine.beginHandshake();
        m_sslInitiated = true;
        reactToHandshakeStatus(m_engine.getHandshakeStatus());
    }

    public void closeEngine()
    {
        if (!m_sslInitiated) return;
        m_engine.closeOutbound();
        m_responder.write(new byte[0]);
    }

    public boolean isEncrypted()
    {
        return m_sslInitiated;
    }
}
