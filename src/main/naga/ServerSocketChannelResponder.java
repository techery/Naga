package naga;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
class ServerSocketChannelResponder extends ChannelResponder implements NIOServerSocket
{
	private long m_totalRefusedConnections;
	private long m_totalAcceptedConnections;
	private long m_totalFailedConnections;
	private long m_totalConnections;
	private volatile ConnectionAcceptor m_connectionAcceptor;
	private ServerSocketObserver m_observer;

	@SuppressWarnings({"ObjectToString"})
	public ServerSocketChannelResponder(NIOService service,
	                                    ServerSocketChannel channel,
	                                    InetSocketAddress address) throws IOException
	{
		super(service, channel, address);
		m_observer = null;
		setConnectionAcceptor(ConnectionAcceptor.ALLOW);
		m_totalRefusedConnections = 0;
		m_totalAcceptedConnections = 0;
		m_totalFailedConnections = 0;
		m_totalConnections = 0;
	}

	public void keyInitialized()
	{
		// Do nothing, since the accept automatically will be set.
	}

	public ServerSocketChannel getChannel()
	{
		return (ServerSocketChannel) super.getChannel();
	}

	/**
	 * Callback to tell the object that there is at least one accept that can be done on the server socket.
	 */
	public void socketReadyForAccept()
	{
		m_totalConnections++;
		SocketChannel socketChannel = null;
		try
		{
			socketChannel = getChannel().accept();
			if (socketChannel == null)
			{
				// This means there actually wasn't any connection waiting,
				// so tick down the number of actual total connections.
				m_totalConnections--;
				return;
			}

			InetSocketAddress address = (InetSocketAddress) socketChannel.socket().getRemoteSocketAddress();
			// Is this connection acceptable?
			if (!m_connectionAcceptor.acceptConnection(address))
			{
				// Connection was refused by the socket owner, so update stats and close connection
				m_totalRefusedConnections++;
				NIOUtils.closeChannelSilently(socketChannel);
				return;
			}

			m_observer.newConnection(getNIOService().registerSocketChannel(socketChannel, address));
			m_totalAcceptedConnections++;
		}
		catch (IOException e)
		{
			// Close channel in case it opened.
			NIOUtils.closeChannelSilently(socketChannel);
			m_totalFailedConnections++;
			m_observer.acceptFailed(e);
		}
	}

	public void notifyWasCancelled()
	{
		close();
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

	public long getTotalAcceptedConnections()
	{
		return m_totalAcceptedConnections;
	}
	
	public void setConnectionAcceptor(ConnectionAcceptor connectionAcceptor)
	{
		m_connectionAcceptor = connectionAcceptor == null ? ConnectionAcceptor.DENY : connectionAcceptor;
	}

	@SuppressWarnings({"CallToPrintStackTrace"})
	private void notifyObserverSocketDied(Exception exception)
	{
		if (m_observer == null) return;
		try
		{
			m_observer.serverSocketDied(exception);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	public void listen(ServerSocketObserver observer)
	{
		if (observer == null) throw new NullPointerException();
		markObserverSet();
		getNIOService().queue(new BeginListenEvent(observer));
	}

	private class BeginListenEvent implements Runnable
	{
		private final ServerSocketObserver m_newObserver;

		private BeginListenEvent(ServerSocketObserver socketObserver)
		{
			m_newObserver = socketObserver;
		}

		public void run()
		{
			m_observer =  m_newObserver;
			if (!isOpen())
			{
				notifyObserverSocketDied(null);
				return;
			}
			addInterest(SelectionKey.OP_ACCEPT);
		}

		@Override
		public String toString()
		{
			return "BeginListen[" + m_newObserver + "]";
		}
	}

	protected void shutdown(Exception e)
	{
		notifyObserverSocketDied(e);
	}

	public ServerSocket socket()
	{
		return getChannel().socket();
	}
}
