package naga;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class forms the basis of the NIO handling in Naga.
 * <p>
 * Common usage is to create a single instance of this service and
 * then run one other the select methods in a loop.
 * <p>
 * Use <code>service.openSocket(host, port)</code> to open a socket to a remote server,
 * and <code>service.openServerSocket(port)</code> to open a server socket locally.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class NIOService
{
	/** The selector used by this service */
	private final Selector m_selector;
	private final Queue<Runnable> m_internalEventQueue;

	/**
	 * Create a new nio service.
	 *
	 * @throws IOException if we failed to open the underlying selector used by the
	 * service.
	 */
	public NIOService() throws IOException
	{
		m_selector = Selector.open();
		m_internalEventQueue = new ConcurrentLinkedQueue<Runnable>();
	}

	/**
	 * Run all waiting NIO requests, blocking indefinitely
	 * until at least one request is handled.
	 *
	 * @throws IOException if there is an IO error waiting for requests.
     * @throws ClosedSelectorException if the underlying selector is closed
	 * (in this case, NIOService#isOpen will return false)
	 */
	public synchronized void selectBlocking() throws IOException
	{
		executeQueue();
		if (m_selector.select() > 0)
		{
			handleSelectedKeys();
		}
		executeQueue();
	}

	/**
	 * Run all waiting NIO requests, returning immediately if
	 * no requests are found.
	 *
	 * @throws IOException if there is an IO error waiting for requests.
     * @throws ClosedSelectorException if the underlying selector is closed.
	 * (in this case, NIOService#isOpen will return false)
	 */
	public synchronized void selectNonBlocking() throws IOException
	{
		executeQueue();
		if (m_selector.selectNow() > 0)
		{
			handleSelectedKeys();
		}
		executeQueue();
	}

	/**
	 * Run all waiting NIO requests, blocking until
	 * at least one request is found, or the method has blocked
	 * for the time given by the timeout value, whatever comes first.
	 *
	 * @param timeout the maximum time to wait for requests.
	 * @throws IllegalArgumentException If the value of the timeout argument is negative.
	 * @throws IOException if there is an IO error waiting for requests.
     * @throws ClosedSelectorException if the underlying selector is closed.
	 * (in this case, NIOService#isOpen will return false)
	 */
	public synchronized void selectBlocking(long timeout) throws IOException
	{
		executeQueue();
		if (m_selector.select(timeout) > 0)
		{
			handleSelectedKeys();
		}
		executeQueue();
	}

	/**
	 * Open a normal socket to the host on the given port returning
	 * a NIOSocket.
	 * <p>
	 * This roughly corresponds to creating a regular socket using new Socket(host, port).
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param host the host we want to connect to.
	 * @param port the port to use for the connection.
	 * @return a NIOSocket object for asynchronous communication.
	 * @throws IOException if registering the new socket failed.
	 */
	public NIOSocket openSocket(String host, int port) throws IOException
	{
		return openSocket(InetAddress.getByName(host), port);
	}

	/**
	 * Open a normal socket to the host on the given port returning
	 * a NIOSocket.
	 * <p>
	 * This roughly corresponds to creating a regular socket using new Socket(inetAddress, port).
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param inetAddress the address we want to connect to.
	 * @param port the port to use for the connection.
	 * @return a NIOSocket object for asynchronous communication.
	 * @throws IOException if registering the new socket failed.
	 */
	public NIOSocket openSocket(InetAddress inetAddress, int port) throws IOException
	{
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(inetAddress, port);
		channel.connect(address);
		return registerSocketChannel(channel, address);
	}

	/**
	 * Open a server socket on the given port.
	 * <p>
	 * This roughly corresponds to using new ServerSocket(port, backlog);
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param port the port to open.
	 * @param backlog the maximum connection backlog (i.e. connections pending accept)
	 * @return a NIOServerSocket for asynchronous connection to the server socket.
	 * @throws IOException if registering the socket fails.
	 */
	public NIOServerSocket openServerSocket(int port, int backlog) throws IOException
	{
		return openServerSocket(new InetSocketAddress(port), backlog);
	}

	/**
	 * Open a server socket on the given port with the default connection backlog.
	 * <p>
	 * This roughly corresponds to using new ServerSocket(port);
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param port the port to open.
	 * @return a NIOServerSocket for asynchronous connection to the server socket.
	 * @throws IOException if registering the socket fails.
	 */
	public NIOServerSocket openServerSocket(int port) throws IOException
	{
		return openServerSocket(port, -1);
	}

	/**
	 * Open a server socket on the address.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param address the address to open.
	 * @param backlog the maximum connection backlog (i.e. connections pending accept)
	 * @return a NIOServerSocket for asynchronous connection to the server socket.
	 * @throws IOException if registering the socket fails.
	 */
	public NIOServerSocket openServerSocket(InetSocketAddress address, int backlog) throws IOException
	{
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.socket().setReuseAddress(true);
		channel.socket().bind(address, backlog);
		channel.configureBlocking(false);
		ServerSocketChannelResponder channelResponder = new ServerSocketChannelResponder(this, channel, address);
		queue(new RegisterChannelEvent(channelResponder));
		return channelResponder;
	}


	/**
	 * Internal method to mark a socket channel for pending registration
	 * and create a NIOSocket wrapper around it.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param socketChannel the socket channel to wrap.
	 * @param address the address for this socket.
	 * @return the NIOSocket wrapper.
	 * @throws IOException if configuring the channel fails, or the underlying selector is closed.
	 */
	NIOSocket registerSocketChannel(SocketChannel socketChannel, InetSocketAddress address) throws IOException
	{
		socketChannel.configureBlocking(false);
		SocketChannelResponder channelResponder = new SocketChannelResponder(this, socketChannel, address);
		queue(new RegisterChannelEvent(channelResponder));
		return channelResponder;
	}

	/**
	 * Internal method to execute events on the internal event queue.
	 * <p>
	 * This method should only ever be called from the NIOService thread.
	 */
	private void executeQueue()
	{
		Runnable event;
		while ((event = m_internalEventQueue.poll()) != null)
		{
			event.run();
		}
	}

	/**
	 * Internal method to handle the key set generated by the internal Selector.
	 * <p>
	 * Will simply remove each entry and handle the key.
	 * <p>
	 * Called on the NIOService thread.
	 */
	private void handleSelectedKeys()
	{
		// Loop through all selected keys and handle each key at a time.
		for (Iterator<SelectionKey> it = m_selector.selectedKeys().iterator(); it.hasNext();)
		{
			// Retrieve the key.
			SelectionKey key = it.next();

			if (key.readyOps() == 0) throw new RuntimeException("Not ready!");
			
			// Remove it from the set so that it is not read again.
			it.remove();

			// Handle actions on this key.
			handleKey(key);
		}
	}


	/**
	 * Internal method to handle a SelectionKey that has changed.
	 * <p>
	 * Will delegate actual actions to the associated ChannelResponder.
	 * <p>
	 * Called on the NIOService thread.
	 * @param key the key to handle.
	 */
	private void handleKey(SelectionKey key)
	{
		ChannelResponder responder = (ChannelResponder) key.attachment();
		try
		{
			if (key.isReadable())
			{
				responder.socketReadyForRead();
			}
			if (key.isWritable())
			{
				responder.socketReadyForWrite();
			}
			if (key.isAcceptable())
			{
				responder.socketReadyForAccept();
			}
			if (key.isConnectable())
			{
				responder.socketReadyForConnect();
			}
		}
		catch (CancelledKeyException e)
		{
			responder.close(e);
			// The key was cancelled and will automatically be removed next select.
		}
	}

	/**
	 * Close the entire service.
	 * <p>
	 * This will disconnect all sockets associated with this service.
	 * <p>
	 * It is not possible to restart the service once closed.
	 * <p>
	 * This method is thread-safe.
	 */
	public void close()
	{
		if (!isOpen()) return;
		queue(new ShutdownEvent());
	}


	/**
	 * Determine if this service is open.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @return true if the service is open, false otherwise.
	 */
	public boolean isOpen()
	{
		return m_selector.isOpen();
	}

	/**
	 * Queues an event on the NIOService queue.
	 * <p>
	 * This method is thread-safe, but should in general not be used by
	 * other applications.
	 *
	 * @param event the event to run on the NIOService-thread.
	 */
	public void queue(Runnable event)
	{
		m_internalEventQueue.add(event);
		m_selector.wakeup();
	}

	/**
	 * Returns a copy of the internal event queue.
	 *
	 * @return a copy of the internal event queue.
	 */
	public Queue<Runnable> getQueue()
	{
		return new LinkedList<Runnable>(m_internalEventQueue);
	}

	/**
	 * A registration class to let registrations occur on the NIOService thread.
	 */
	private class RegisterChannelEvent implements Runnable
	{
		private final ChannelResponder m_channelResponder;

		private RegisterChannelEvent(ChannelResponder channelResponder)
		{
			m_channelResponder = channelResponder;
		}

		public void run()
		{
			try
			{
				SelectionKey key = m_channelResponder.getChannel().register(m_selector, 0);
				m_channelResponder.setKey(key);
				key.attach(m_channelResponder);
			}
			catch (Exception e)
			{
				m_channelResponder.close(e);
			}
		}

		@Override
		public String toString()
		{
			return "Register[" + m_channelResponder + "]";
		}
	}

	/**
	 * Shutdown class to let shutdown happen on the NIOService thread.
	 */
	private class ShutdownEvent implements Runnable
	{
		public void run()
		{
			if (!isOpen()) return;
			for (SelectionKey key : m_selector.keys())
			{
				try
				{
					NIOUtils.cancelKeySilently(key);
					((ChannelResponder) key.attachment()).close();
				}
				catch (Exception e)
				{
					// Swallow exceptions.
				}
			}
			try
			{
				m_selector.close();
			}
			catch (IOException e)
			{
				// Swallow exceptions.
			}
		}
	}
}
