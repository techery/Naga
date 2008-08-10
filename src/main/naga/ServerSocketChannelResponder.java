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
public class ServerSocketChannelResponder implements ChannelResponder, NIOServerSocket
{
	private final static ServerSocketObserver NULLOBSERVER = new ServerSocketObserverAdapter();

	private final ServerSocketChannel m_channel;
	private SelectionKey m_key;
	private final NIOService m_nioService;
	private boolean m_alive;
	private long m_totalRefusedConnections;
	private long m_totalAcceptedConnections;
	private long m_totalFailedConnections;
	private long m_totalConnections;
	private String m_localAddress;
	private int m_localPort;
	private ConnectionAcceptor m_connectionAcceptor;
	private ServerSocketObserver m_observer;

	@SuppressWarnings({"ObjectToString"})
	public ServerSocketChannelResponder(NIOService service,
	                                    ServerSocketChannel channel) throws IOException
	{
		m_channel = channel;
		m_key = NIOUtils.NULL_KEY;
		m_nioService = service;
		m_alive = true;
		setConnectionAcceptor(null);
		m_localPort = m_channel.socket().getLocalPort();
		m_localAddress = m_channel.socket().getLocalSocketAddress().toString();
		m_totalRefusedConnections = 0;
		m_totalAcceptedConnections = 0;
		m_totalFailedConnections = 0;
		m_totalConnections = 0;
	}

	public void setKey(SelectionKey key)
	{
		if (m_key != NIOUtils.NULL_KEY) throw new IllegalStateException("Tried to set selection key twice");
		m_key = key;
		m_key.interestOps(SelectionKey.OP_ACCEPT);
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

	private void socketDied()
	{
		if (m_alive)
		{
			m_alive = false;
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

	public boolean isAlive()
	{
		return m_alive;
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
		return m_localAddress + ":" + m_localPort + (m_alive ? "" : "<CLOSED>");
	}

	public NIOService getService()
	{
		return m_nioService;
	}

	public void setConnectionAcceptor(ConnectionAcceptor connectionAcceptor)
	{
		m_connectionAcceptor = connectionAcceptor == null ? ConnectionAcceptor.DENY : connectionAcceptor;
	}

	public void setObserver(ServerSocketObserver observer)
	{
		m_observer = observer == null ? NULLOBSERVER : observer;
	}
}
