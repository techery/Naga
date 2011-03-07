package naga.ssl;

import naga.NIOSocket;
import naga.PacketReader;
import naga.PacketWriter;
import naga.SocketObserver;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public class SSLSocketChannelResponder implements NIOSocketSSL
{
    private final NIOSocket m_wrappedSocket;
    private final SSLPacketHandler m_packetHandler;

    public SSLSocketChannelResponder(NIOSocket wrappedSocket, SSLEngine engine, boolean client) throws SSLException
    {
        m_wrappedSocket = wrappedSocket;
        m_packetHandler = new SSLPacketHandler(engine, m_wrappedSocket);
        m_wrappedSocket.setPacketReader(m_packetHandler);
        m_wrappedSocket.setPacketWriter(m_packetHandler);
        engine.setUseClientMode(client);
        engine.beginHandshake();
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
        m_wrappedSocket.setPacketReader(packetReader);
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
        m_wrappedSocket.listen(socketObserver);
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
}
