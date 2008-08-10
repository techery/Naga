package naga;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class NIOService
{
	private final Selector m_selector;
	private final Queue<ChannelResponder> m_socketsPendingRegistration;

	public NIOService() throws IOException
	{
		m_selector = Selector.open();
		m_socketsPendingRegistration = new ConcurrentLinkedQueue<ChannelResponder>();
	}

	public void selectBlocking() throws IOException
	{
		registerChannelResponder();
		if (m_selector.select() > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
	}

	public void selectNonBlocking() throws IOException
	{
		registerChannelResponder();
		if (m_selector.selectNow() > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
	}

	public void selectBlocking(long timeout) throws IOException
	{
		registerChannelResponder();
		if (m_selector.select(timeout) > 0)
		{
			handleSelectedKeys();
		}
		registerChannelResponder();
	}

	public NIOSocket openSocket(String host, int port) throws IOException
	{
		return openSocket(new InetSocketAddress(host, port));
	}

	public NIOSocket openSocket(InetSocketAddress adress) throws IOException
	{
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(adress);
		return registerSocketChannel(channel);
	}

	public NIOServerSocket openServerSocket(int port, int backlog) throws IOException
	{
		return openServerSocket(new InetSocketAddress(port), backlog);
	}

	public NIOServerSocket openServerSocket(int port) throws IOException
	{
		return openServerSocket(port, -1);
	}

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


	public NIOSocket registerSocketChannel(SocketChannel socketChannel) throws IOException
	{
		socketChannel.configureBlocking(false);
		SocketChannelResponder keyHolder = new SocketChannelResponder(socketChannel);
		m_socketsPendingRegistration.add(keyHolder);
		m_selector.wakeup();
		return keyHolder;
	}

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

	public void shutdown()
	{
		if (!m_selector.isOpen()) return;

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
}
