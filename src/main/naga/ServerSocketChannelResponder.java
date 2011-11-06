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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Christoffer Lerno
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
        addInterest(SelectionKey.OP_ACCEPT);
	}

	public ServerSocketChannel getChannel()
	{
		return (ServerSocketChannel) super.getChannel();
	}

    /**
     * Override point for substituting NIOSocket wrappers.
     *
     * @param channel the channel to register.
     * @param address the address associated with the channel.
     * @return A new NIOSocket
     * @throws IOException if registration failed.
     */
    NIOSocket registerSocket(SocketChannel channel, InetSocketAddress address) throws IOException
    {
        return getNIOService().registerSocketChannel(channel, address);
    }

    private void notifyNewConnection(NIOSocket socket)
    {
        try
        {
            if (m_observer != null) m_observer.newConnection(socket);
        }
        catch (Exception e)
        {
            getNIOService().notifyException(e);
            socket.close();
        }
    }

    private void notifyAcceptFailed(IOException theException)
    {
        try
        {
            if (m_observer != null) m_observer.acceptFailed(theException);
        }
        catch (Exception e)
        {
            getNIOService().notifyException(e);
        }
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
            notifyNewConnection(registerSocket(socketChannel, address));
			m_totalAcceptedConnections++;
		}
		catch (IOException e)
		{
			// Close channel in case it opened.
			NIOUtils.closeChannelSilently(socketChannel);
			m_totalFailedConnections++;
            notifyAcceptFailed(e);
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

	private void notifyObserverSocketDied(Exception exception)
	{
		try
		{
			if (m_observer != null) m_observer.serverSocketDied(exception);
		}
		catch (Exception e)
		{
            getNIOService().notifyException(e);
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
