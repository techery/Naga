package naga;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * SSL-implementation on top of NIOSocket, wrapping all calls to the socket.
 *
 * @author Christoffer Lerno
 */
class SSLSocketChannelResponder implements NIOSocketSSL, SocketObserver
{
    private final NIOSocket m_wrappedSocket;
    private final SSLPacketHandler m_packetHandler;
    private final NIOService m_nioService;
    private SocketObserver m_observer;

    public SSLSocketChannelResponder(NIOService nioService, NIOSocket wrappedSocket, SSLEngine engine, boolean client) throws SSLException
    {
        m_nioService = nioService;
        m_wrappedSocket = wrappedSocket;
        m_packetHandler = new SSLPacketHandler(engine, m_wrappedSocket, this);
        m_wrappedSocket.setPacketReader(m_packetHandler);
        m_wrappedSocket.setPacketWriter(m_packetHandler);
        engine.setUseClientMode(client);
    }

    public void beginHandshake() throws SSLException
    {
        if (getSSLEngine().getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) throw new IllegalStateException("Tried to start handshake during handshake.");
        m_packetHandler.begin();
    }

    public boolean isEncrypted()
    {
        return m_packetHandler.isEncrypted();
    }

    public SSLEngine getSSLEngine()
    {
        return m_packetHandler.getSSLEngine();
    }


    public boolean write(byte[] packet)
    {
        return m_wrappedSocket.write(packet);
    }

    public boolean write(byte[] packet, Object tag)
    {
        return m_wrappedSocket.write(packet, tag);
    }

    public void queue(Runnable runnable)
    {
        m_wrappedSocket.queue(runnable);
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
        m_packetHandler.closeEngine();
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
        try
        {
            if (m_observer != null) m_observer.connectionBroken(this, e);
        }
        catch (Exception ex)
        {
            m_nioService.notifyException(e);
        }
        m_wrappedSocket.close();
    }

    public void connectionOpened(NIOSocket nioSocket)
    {
        try
        {
            if (m_observer != null) m_observer.connectionOpened(this);
        }
        catch (Exception e)
        {
            m_nioService.notifyException(e);
        }
    }
    
    public void connectionBroken(NIOSocket nioSocket, Exception exception)
    {
        try
        {
            if (m_observer != null) m_observer.connectionBroken(this, exception);
        }
        catch (Exception e)
        {
            m_nioService.notifyException(e);
        }
    }

    public void packetReceived(NIOSocket socket, byte[] packet)
    {
        try
        {
            if (m_observer != null) m_observer.packetReceived(this, packet);
        }
        catch (Exception e)
        {
            m_nioService.notifyException(e);
        }
    }

    public void packetSent(NIOSocket socket, Object tag)
    {
        try
        {
            if (m_observer != null) m_observer.packetSent(this, tag);
        }
        catch (Exception e)
        {
            m_nioService.notifyException(e);
        }
    }

}
