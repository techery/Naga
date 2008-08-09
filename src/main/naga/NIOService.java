package naga;

import java.nio.channels.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class NIOService
{
	private final Selector m_selector;

	public NIOService() throws IOException
	{
		m_selector = Selector.open();
	}

	public void selectBlocking() throws IOException
	{
		if (m_selector.select() > 0)
		{
			handleSelectedKeys();
		}
	}

	public void selectNonBlocking() throws IOException
	{
		if (m_selector.selectNow() > 0)
		{
			handleSelectedKeys();
		}
	}

	public void selectBlocking(long timeout) throws IOException
	{
		if (m_selector.select(timeout) > 0)
		{
			handleSelectedKeys();
		}
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
		SelectionKey key = channel.register(m_selector, 0);
		ServerSocketChannelResponder keyHolder = new ServerSocketChannelResponder(this, channel, key);
		key.attach(keyHolder);
		return keyHolder;
	}


	public NIOSocket registerSocketChannel(SocketChannel socketChannel) throws IOException
	{
		socketChannel.configureBlocking(false);
		SelectionKey key = socketChannel.register(m_selector, 0);
		SocketChannelResponder keyHolder = new SocketChannelResponder(socketChannel, key);
		key.attach(keyHolder);
		return keyHolder;
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
