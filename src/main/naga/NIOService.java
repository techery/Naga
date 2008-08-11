package naga;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
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
	/** A queue with sockets that are waiting to be registered with the selector */
	private final Queue<ChannelResponder> m_socketsPendingRegistration;

	/**
	 * Create a new nio service.
	 *
	 * @throws IOException if we failed to open the underlying selector used by the
	 * service.
	 */
	public NIOService() throws IOException
	{
		m_selector = Selector.open();
		m_socketsPendingRegistration = new ConcurrentLinkedQueue<ChannelResponder>();
	}

	/**
	 * Run all waiting NIO requests, blocking indefinitely
	 * until at least one request is handled.
	 *
	 * @throws IOException if there is an IO error waiting for requests.
     * @throws ClosedSelectorException if the underlying selector is closed
	 * (in this case, NIOService#isOpen will return false)
	 */
	public void selectBlocking() throws IOException
	{
		registerChannelResponder();
		if (m_selector.select() > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
	}

	/**
	 * Run all waiting NIO requests, returning immediately if
	 * no requests are found.
	 *
	 * @throws IOException if there is an IO error waiting for requests.
     * @throws ClosedSelectorException if the underlying selector is closed.
	 * (in this case, NIOService#isOpen will return false)
	 */
	public void selectNonBlocking() throws IOException
	{
		registerChannelResponder();
		if (m_selector.selectNow() > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
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
	public void selectBlocking(long timeout) throws IOException
	{
		registerChannelResponder();
		if (m_selector.select(timeout) > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
	}

	/**
	 * Open a normal socket to the host on the given port returning
	 * a NIOSocket.
	 * <p>
	 * This roughly corresponds to creating a regular socket using new Socket(host, port).
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
		channel.connect(new InetSocketAddress(inetAddress, port));
		return registerSocketChannel(channel);
	}

	/**
	 * Open a server socket on the given port.
	 * <p>
	 * This roughly corresponds to using new ServerSocket(port, backlog);
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
		ServerSocketChannelResponder keyHolder = new ServerSocketChannelResponder(this, channel);
		m_socketsPendingRegistration.add(keyHolder);
		m_selector.wakeup();
		return keyHolder;
	}


	/**
	 * Internal method to mark a socket channel for pending registration
	 * and create a NIOSocket wrapper around it.
	 *
	 * @param socketChannel the socket channel to wrap.
	 * @return the NIOSocket wrapper.
	 * @throws IOException if configuring the channel fails, or the underlying selector is closed.
	 */
	NIOSocket registerSocketChannel(SocketChannel socketChannel) throws IOException
	{
		socketChannel.configureBlocking(false);
		SocketChannelResponder keyHolder = new SocketChannelResponder(socketChannel);
		m_socketsPendingRegistration.add(keyHolder);
		m_selector.wakeup();
		return keyHolder;
	}

	/**
	 * Internal method to register any pending channel responders.
	 * <p>
	 * If registration fails then channel responder will be informed.
	 */
	private void registerChannelResponder()
	{
		ChannelResponder channelResponder;
		while ((channelResponder = m_socketsPendingRegistration.poll()) != null)
		{
			try
			{
				SelectionKey key = channelResponder.getChannel().register(m_selector, 0);
				channelResponder.setKey(key);
				key.attach(channelResponder);
			}
			catch (Exception e)
			{
				channelResponder.notifyWasCancelled();
			}
		}
	}

	/**
	 * Internal method to handle the key set generated by the internal Selector.
	 * <p>
	 * Will simply remove each entry and handle the key.
	 */
	private void handleSelectedKeys()
	{
		// Loop through all selected keys and handle each key at a time.
		for (Iterator<SelectionKey> it = m_selector.selectedKeys().iterator(); it.hasNext();)
		{
			// Retrieve the key.
			SelectionKey key = it.next();

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
	 *
	 * @param key the key to handle.
	 */
	private void handleKey(SelectionKey key)
	{
		try
		{
			if (key.isReadable())
			{
				((ChannelResponder) key.attachment()).notifyCanRead();
			}
			if (key.isWritable())
			{
				((ChannelResponder) key.attachment()).notifyCanWrite();
			}
			if (key.isAcceptable())
			{
				((ChannelResponder) key.attachment()).notifyCanAccept();
			}
			if (key.isConnectable())
			{
				((ChannelResponder) key.attachment()).notifyCanConnect();
			}
		}
		catch (CancelledKeyException e)
		{
			((ChannelResponder) key.attachment()).notifyWasCancelled();
			// The key was cancelled and will automatically be removed next select.
		}
	}

	/**
	 * Close the entire service.
	 * <p>
	 * This will disconnect all sockets associated with this service.
	 * <p>
	 * It is not possible to restart the service once closed.
	 */
	public void close()
	{
		if (!isOpen()) return;

		for (SelectionKey key : m_selector.keys())
		{
			try
			{
				NIOUtils.cancelKeySilently(key);
				((ChannelResponder) key.attachment()).notifyWasCancelled();
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

	/**
	 * Determine if this service is open.
	 *
	 * @return true if the service is open, false otherwise.
	 */
	public boolean isOpen()
	{
		return m_selector.isOpen();
	}
}
