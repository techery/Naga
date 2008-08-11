package naga;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
class ServerSocketChannelResponder implements ChannelResponder, NIOServerSocket
{
	private final ServerSocketChannel m_channel;
	private SelectionKey m_key;
	private final NIOService m_nioService;
	private long m_totalRefusedConnections;
	private long m_totalAcceptedConnections;
	private long m_totalFailedConnections;
	private long m_totalConnections;
	private String m_localAddress;
	private int m_localPort;
	private ConnectionAcceptor m_connectionAcceptor;
	private ServerSocketObserver m_observer;
	private boolean m_disconnectReported;

	@SuppressWarnings({"ObjectToString"})
	public ServerSocketChannelResponder(NIOService service,
	                                    ServerSocketChannel channel) throws IOException
	{
		m_channel = channel;
		m_key = null;
		m_nioService = service;
		m_disconnectReported = false;
		m_observer = null;
		setConnectionAcceptor(null);
		m_localPort = m_channel.socket().getLocalPort();
		m_localAddress = m_channel.socket().getLocalSocketAddress().toString();
		m_totalRefusedConnections = 0;
		m_totalAcceptedConnections = 0;
		m_totalFailedConnections = 0;
		m_totalConnections = 0;
	}

	public synchronized void setKey(SelectionKey key)
	{
		if (m_key != null) throw new IllegalStateException("Tried to set selection key twice");
		m_key = key;
		if (!m_channel.isOpen())
		{
			// In case the channel already was closed when we
			NIOUtils.cancelKeySilently(m_key);
			return;
		}
		startAcceptIfReady();
	}

	private void startAcceptIfReady()
	{
		if (m_key != null && m_observer != null)
		{
			m_key.interestOps(SelectionKey.OP_ACCEPT);
		}
	}

	public SelectableChannel getChannel()
	{
		return m_channel;
	}

	/**
	 * Callback to tell the object that there is at least one accept that can be done on the server socket.
	 */
	public void notifyCanAccept()
	{
		m_totalConnections++;
		SocketChannel socketChannel = null;
		try
		{
			socketChannel = m_channel.accept();
			if (socketChannel == null)
			{
				// This means there actually wasn't any connection waiting,
				// so tick down the number of actual total connections.
				m_totalConnections--;
				return;
			}

			// Is this connection acceptable?
			if (!m_connectionAcceptor.acceptConnection(socketChannel.socket().getInetAddress().getHostAddress()))
			{
				// Connection was refused by the socket owner, so update stats and close connection
				m_totalRefusedConnections++;
				NIOUtils.closeChannelSilently(socketChannel);
				return;
			}

			m_observer.newConnection(m_nioService.registerSocketChannel(socketChannel));
			m_totalAcceptedConnections++;
		}
		catch (IOException e)
		{
			// Close channel in case it opened.
			NIOUtils.closeChannelSilently(socketChannel);
			m_totalFailedConnections++;
			m_observer.notifyAcceptingConnectionFailed(e);
		}
	}

	public void notifyCanConnect()
	{
		throw new UnsupportedOperationException("Not supported for server sockets.");
	}

	public void notifyCanRead()
	{
		throw new UnsupportedOperationException("Not supported for server sockets.");
	}

	public void notifyCanWrite()
	{
		throw new UnsupportedOperationException("Not supported for server sockets.");
	}

	private synchronized void socketDied()
	{
		if (!m_disconnectReported)
		{
			m_disconnectReported = true;
			m_observer.notifyServerSocketDied();
		}
	}

	public void notifyWasCancelled()
	{
		socketDied();
	}

	public void close()
	{
		NIOUtils.closeKeyAndChannelSilently(m_key, m_channel);
		socketDied();
	}

	public long getTotalRefusedConnections()
	{
		return m_totalRefusedConnections;
	}

	public long getTotalConnections()
	{
		return m_totalConnections;
	}

	public long getTotalFailedConnections()
	{
		return m_totalFailedConnections;
	}

	public boolean isOpen()
	{
		return m_channel.isOpen();
	}

	public long getTotalAcceptedConnections()
	{
		return m_totalAcceptedConnections;
	}

	public String getLocalAddress()
	{
		return m_localAddress;
	}

	public int getLocalPort()
	{
		return m_localPort;
	}

	@Override
	public String toString()
	{
		return m_localAddress + ":" + m_localPort;
	}

	public NIOService getService()
	{
		return m_nioService;
	}

	public void setConnectionAcceptor(ConnectionAcceptor connectionAcceptor)
	{
		m_connectionAcceptor = connectionAcceptor == null ? ConnectionAcceptor.DENY : connectionAcceptor;
	}

	public void listen(ServerSocketObserver observer)
	{
		if (m_observer != null) throw new IllegalArgumentException("There is already a listener attached to this socket");
		m_observer = observer;
		startAcceptIfReady();
	}
}
