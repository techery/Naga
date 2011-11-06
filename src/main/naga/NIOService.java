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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
 * Use {@link naga.NIOService#openSocket(String, int)} to open a socket to a remote server,
 * and {@link naga.NIOService#openServerSocket(int)}  to open a server socket locally.
 * <p>
 * Note that the NIOServerSocket by default opens in a "refuse connections" state. To
 * start accepting players, the socket's acceptor must first be set to accept connections.
 * See documentation for openServerSocket for more details.
 * <p>
 * Example use:
 * <p>
 * Using the server socket:
 * <code><pre>
 * NIOService service = new NIOService;
 * NIOServerSocket serverSocket = service.openServerSocket(1234);
 * serverSocket.setConnectionAcceptor(myAcceptor);
 * serverSocket.listen(myObserver);
 * </pre></code>
 * Using regular sockets:
 * <code><pre>
 * NIOService service = new NIOService;
 * NIOSocket socket = service.openSocket("www.google.com", 1234);
 * socket.listen(myObserver);
 * // Asynchronous write by default:
 * socket.write("Some message".getBytes());
 * </pre></code>
 *
 * @author Christoffer Lerno
 */
public class NIOService
{
    public final static int DEFAULT_IO_BUFFER_SIZE = 64 * 1024;

	/** The selector used by this service */
	private final Selector m_selector;
	private final Queue<Runnable> m_internalEventQueue;
    private ByteBuffer m_sharedBuffer;
    private ExceptionObserver m_exceptionObserver;

	/**
	 * Create a new nio service with default buffer size (64kb)
	 *
	 * @throws IOException if we failed to open the underlying selector used by the
	 * service.
	 */
	public NIOService() throws IOException
	{
        this(DEFAULT_IO_BUFFER_SIZE);
	}

    /**
     * Create a new nio service.
     *
     * @param ioBufferSize the maximum buffer size.
     * @throws IOException if we failed to open the underlying selector used by the
     * service.
     * @throws IllegalArgumentException if the buffer size is less than 256 bytes.
     */
    public NIOService(int ioBufferSize) throws IOException
	{
		m_selector = Selector.open();
		m_internalEventQueue = new ConcurrentLinkedQueue<Runnable>();
        m_exceptionObserver = ExceptionObserver.DEFAULT;
        setBufferSize(ioBufferSize);
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
	 * <em>This method is thread-safe.</em>
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
     * Open a socket to the host on the given port returning
     * a NIOSocketSSL.
     * <p>
     * This roughly corresponds to creating a regular socket using new SSLSocket(host, port).
     * <p>
     * <em>This method is thread-safe.</em>
     *
     * @param sslEngine the SSL engine to use for SSL-negotiation.
     * @param host the host we want to connect to.
     * @param port the port to use for the connection.
     * @return a NIOSocket object for asynchronous communication.
     * @throws IOException if registering the new socket failed.
     */
    public NIOSocket openSSLSocket(SSLEngine sslEngine, String host, int port) throws IOException
    {
        return openSSLSocket(sslEngine, InetAddress.getByName(host), port);
    }

	/**
	 * Open a normal socket to the host on the given port returning
	 * a NIOSocket.
	 * <p>
	 * This roughly corresponds to creating a regular socket using new Socket(inetAddress, port).
	 * <p>
	 * <em>This method is thread-safe.</em>
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
     * Open a socket to the host on the given port returning
     * a NIOSocketSSL.
     * <p>
     * This roughly corresponds to creating a regular socket using new SSLSocket(inetAddress, port).
     * <p>
     * <em>This method is thread-safe.</em>
     *
     * @param sslEngine the SSL engine to use for SSL-negotiation.
     * @param inetAddress the address we want to connect to.
     * @param port the port to use for the connection.
     * @return a NIOSocketSSL object for asynchronous communication.
     * @throws IOException if registering the new socket failed.
     */
    public NIOSocketSSL openSSLSocket(SSLEngine sslEngine, InetAddress inetAddress, int port) throws IOException
    {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(inetAddress, port);
        channel.connect(address);
        return new SSLSocketChannelResponder(this, registerSocketChannel(channel, address), sslEngine, true);
    }



    /**
     * Open a server socket on the given port.
     * <p>
     * This roughly corresponds to using new ServerSocket(port, backlog);
     * <p>
     * <em>This method is thread-safe.</em>
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
     * Open an SSL server socket on the given port.
     * <p>
     * This roughly corresponds to using new SSLServerSocket(port, backlog);
     * <p>
     * <em>This method is thread-safe.</em>
     *
     * @param sslContext the SSLContext to use for SSL-negotiation.
     * @param port the port to open.
     * @param backlog the maximum connection backlog (i.e. connections pending accept)
     * @return a NIOServerSocket for asynchronous connection to the server socket.
     * @throws IOException if registering the socket fails.
     */
    public NIOServerSocketSSL openSSLServerSocket(SSLContext sslContext, int port, int backlog) throws IOException
    {
        return openSSLServerSocket(sslContext, new InetSocketAddress(port), backlog);
    }

	/**
	 * Open a server socket on the given port with the default connection backlog.
	 * <p>
	 * This roughly corresponds to using new ServerSocket(port);
	 * <p>
	 * <em>This method is thread-safe.</em>
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
     * Open an SSL server socket on the given port with the default connection backlog.
     * <p>
     * This roughly corresponds to using new SSLServerSocket(port);
     * <p>
     * <em>This method is thread-safe.</em>
     *
     * @param sslContext the SSLContext to use for SSL-negotiation.
     * @param port the port to open.
     * @return a NIOServerSocket for asynchronous connection to the server socket.
     * @throws IOException if registering the socket fails.
     */
    public NIOServerSocketSSL openSSLServerSocket(SSLContext sslContext, int port) throws IOException
    {
        return openSSLServerSocket(sslContext, port, -1);
    }

	/**
	 * Open an SSL server socket on the address.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
     * @param sslContext the SSLContext to use for SSL-negotiation.
	 * @param address the address to open.
	 * @param backlog the maximum connection backlog (i.e. connections pending accept)
	 * @return a NIOServerSocket for asynchronous connection to the server socket.
	 * @throws IOException if registering the socket fails.
	 */
	public NIOServerSocketSSL openSSLServerSocket(SSLContext sslContext, InetSocketAddress address, int backlog) throws IOException
	{
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.socket().setReuseAddress(true);
		channel.socket().bind(address, backlog);
		channel.configureBlocking(false);
		SSLServerSocketChannelResponder channelResponder = new SSLServerSocketChannelResponder(sslContext, this, channel, address);
		queue(new RegisterChannelEvent(channelResponder));
		return channelResponder;
	}


    /**
     * Open a server socket on the address.
     * <p>
     * <em>This method is thread-safe.</em>
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
	 * <em>This method is thread-safe.</em>
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
            try
            {
                event.run();
            }
            catch (Throwable t)
            {
                notifyException(t);
            }
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

			// Remove it from the set so that it is not read again.
			it.remove();

			// Handle actions on this key.
            try
            {
                handleKey(key);
            }
            catch (Throwable t)
            {
                notifyException(t);
            }
		}
	}

    /**
     * Set the new shared buffer size.
     * <p>
     * <em>This method is *not* thread-safe.</em>
     * @param newBufferSize the new buffer size.
     * @throws IllegalArgumentException if the new size is less than 256 bytes.
     */
    public void setBufferSize(int newBufferSize)
    {
        if (newBufferSize < 256) throw new IllegalArgumentException("The buffer must at least hold 256 bytes");
        m_sharedBuffer = ByteBuffer.allocate(newBufferSize);
    }

    /**
     * Returns the new shared buffer size.
     * <p/>
     * <em>This method is *not* thread-safe.</em>
     *
     * @return the current buffer size, which is the largest packet that can be read.
     */
    public int getBufferSize()
    {
        return m_sharedBuffer.capacity();
    }


    /**
     * Returns the shared byte buffer. This is shared between all users of the service to avoid allocating
     * a huge number of byte buffers.
     *
     * @return the shared byte buffer.
     */
    ByteBuffer getSharedBuffer()
    {
        return m_sharedBuffer;
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
	 * <em>This method is thread-safe.</em>
	 */
	public void close()
	{
		if (!isOpen()) return;
		queue(new ShutdownEvent());
	}


	/**
	 * Determine if this service is open.
	 * <p>
	 * <em>This method is thread-safe.</em>
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
		wakeup();
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
	 * Runs wakeup on the selector, causing any blocking select to be released.
	 */
	public void wakeup()
	{
		m_selector.wakeup();
	}

    /**
     * Updates the exception observer for the NIOService.
     *
     * @param exceptionObserver the new exception observer, if this is null, logging will be directed to stderr.
     */
    public void setExceptionObserver(ExceptionObserver exceptionObserver)
    {
        final ExceptionObserver newExceptionObserver = exceptionObserver == null ? ExceptionObserver.DEFAULT : exceptionObserver;
        queue(new Runnable() {
            public void run()
            {
                m_exceptionObserver = newExceptionObserver;
            }
        });
    }

    /**
     * Logs an exception using the exception observer. This is mainly for use by the Naga classes.
     * <p>
     * Should only be used on the NIOService thread.
     *
     * @param t the exception thrown.
     */
    public void notifyException(Throwable t)
    {
        try
        {
            m_exceptionObserver.notifyExceptionThrown(t);
        }
        catch (Exception e)
        {
            System.err.println("Failed to log the following exception to the exception observer:");
            System.err.println(e);
            e.printStackTrace();
        }
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
				SelectionKey key = m_channelResponder.getChannel().register(m_selector, m_channelResponder.getChannel().validOps());
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
