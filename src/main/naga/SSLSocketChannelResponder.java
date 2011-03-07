package naga;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SSL-implementation on top of NIOSocket, wrapping all calls to the socket.
 *
 * @author Christoffer Lerno
 */
public class SSLSocketChannelResponder implements NIOSocketSSL, SocketObserver
{
    private final NIOSocket m_wrappedSocket;
    private final SSLPacketHandler m_packetHandler;
    private final ConcurrentLinkedQueue<Object> m_startQueue;
    private final AtomicBoolean m_isListening;
    private SocketObserver m_observer;

    public SSLSocketChannelResponder(NIOSocket wrappedSocket, SSLEngine engine, boolean client) throws SSLException
    {
        m_wrappedSocket = wrappedSocket;
        m_packetHandler = new SSLPacketHandler(engine, m_wrappedSocket, this);
        m_wrappedSocket.setPacketReader(m_packetHandler);
        m_wrappedSocket.setPacketWriter(m_packetHandler);
        m_startQueue = new ConcurrentLinkedQueue<Object>();
        m_isListening = new AtomicBoolean(false);
        engine.setUseClientMode(client);
        engine.beginHandshake();
    }

    public SSLEngine getSSLEngine()
    {
        return m_packetHandler.getSSLEngine();
    }

    private void emptyStartQueue(boolean isListening)
    {
        if (isListening)
        {
            Object o;
            while ((o = m_startQueue.poll()) != null)
            {
                if (o instanceof byte[])
                {
                    m_wrappedSocket.write((byte[]) o);
                }
                else if (o instanceof Runnable)
                {
                    m_wrappedSocket.queue((Runnable)o);
                }
                else
                {
                    m_wrappedSocket.write((byte[])(((Object[])o)[0]), ((Object[])o)[1]);
                }
            }
        }
    }

    public boolean write(byte[] packet)
    {
        if (!m_isListening.get())
        {
            m_startQueue.add(packet);
            emptyStartQueue(m_isListening.get());
            return true;
        }
        else
        {
            return m_wrappedSocket.write(packet);
        }
    }

    public boolean write(byte[] packet, Object tag)
    {
        if (!m_isListening.get())
        {
            m_startQueue.add(new Object[] { packet,  tag });
            emptyStartQueue(m_isListening.get());
            return true;
        }
        else
        {
            return m_wrappedSocket.write(packet, tag);
        }
    }

    public void queue(Runnable runnable)
    {
        if (!m_isListening.get())
        {
            m_startQueue.add(runnable);
            emptyStartQueue(m_isListening.get());
        }
        else
        {
            m_wrappedSocket.queue(runnable);
        }
    }

    public long getBytesRead()
    {
        return m_wrappedSocket.getBytesRead();
    }

    public long getBytesWritten()
    {
        return m_wrappedSocket.getBytesWritten();
    }

    public long getTimeOpen()
    {
        return m_wrappedSocket.getTimeOpen();
    }

    public long getWriteQueueSize()
    {
        return m_wrappedSocket.getWriteQueueSize();
    }

    public int getMaxQueueSize()
    {
        return m_wrappedSocket.getMaxQueueSize();
    }

    public void setMaxQueueSize(int maxQueueSize)
    {
        m_wrappedSocket.setMaxQueueSize(maxQueueSize);
    }

    public void setPacketReader(PacketReader packetReader)
    {
        m_packetHandler.setReader(packetReader);
    }

    public void setPacketWriter(final PacketWriter packetWriter)
    {
        m_wrappedSocket.queue(new Runnable()
        {
            public void run()
            {
                m_packetHandler.setWriter(packetWriter);
            }
        });
    }

    public void listen(SocketObserver socketObserver)
    {
        m_observer = socketObserver;
        m_wrappedSocket.listen(this);
    }

    public void closeAfterWrite()
    {
        m_wrappedSocket.closeAfterWrite();
    }

    public Socket socket()
    {
        return m_wrappedSocket.socket();
    }

    public void close()
    {
        m_wrappedSocket.close();
    }

    public InetSocketAddress getAddress()
    {
        return m_wrappedSocket.getAddress();
    }

    public boolean isOpen()
    {
        return m_wrappedSocket.isOpen();
    }

    public String getIp()
    {
        return m_wrappedSocket.getIp();
    }

    public int getPort()
    {
        return m_wrappedSocket.getPort();
    }

    public Object getTag()
    {
        return m_wrappedSocket.getTag();
    }

    public void setTag(Object tag)
    {
        m_wrappedSocket.setTag(tag);
    }

    void closeDueToSSLException(SSLException e)
    {
        if (m_observer != null) m_observer.connectionBroken(this, e);
        m_wrappedSocket.close();
    }

    public void connectionOpened(NIOSocket nioSocket)
    {
        try
        {
            m_packetHandler.begin();
        }
        catch (SSLException e)
        {
            closeDueToSSLException(e);
        }
    }
    
    public void connectionBroken(NIOSocket nioSocket, Exception exception)
    {
        if (m_observer != null) m_observer.connectionBroken(this, exception);
    }

    public void packetReceived(NIOSocket socket, byte[] packet)
    {
        if (m_isListening.get() && m_observer != null) m_observer.packetReceived(this, packet);
    }

    public void packetSent(NIOSocket socket, Object tag)
    {
        if (m_isListening.get() && m_observer != null) m_observer.packetSent(this, tag);
    }

    public void handshakeCompleted()
    {
        emptyStartQueue(true);
        m_isListening.set(true);
        emptyStartQueue(true);
        if (m_observer != null) m_observer.connectionOpened(this);
    }
}
