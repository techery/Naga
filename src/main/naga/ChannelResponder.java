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

import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * The base class for socket and server socket responders.
 * <p>
 * Handles basic functionality such as presenting IP and port,
 * handling of key and channel as well as closing the socket.
 *
 * @author Christoffer Lerno
 */
abstract class ChannelResponder implements NIOAbstractSocket
{
	private final NIOService m_service;
	private final String m_ip;
	private final InetSocketAddress m_address;
	private final int m_port;
	private final SelectableChannel m_channel;
	private volatile boolean m_open;
	private volatile SelectionKey m_key;
	private volatile int m_interestOps;
	private boolean m_observerSet;
    private Object m_tag;

	/**
	 * Creates a new channel responder.
	 *
	 * @param service the NIOService this responder belongs to.
	 * @param channel the channel handled by this responder.
	 * @param address the address this channel is associated with.
	 */
	protected ChannelResponder(NIOService service, SelectableChannel channel, InetSocketAddress address)
	{
		m_channel = channel;
		m_service = service;
		m_open = true;
		m_key = null;
		m_interestOps = 0;
		m_observerSet = false;
		m_address = address;
		m_ip = address.getAddress().getHostAddress();
		m_port = address.getPort();
        m_tag = null;
	}


	public InetSocketAddress getAddress()
	{
		return m_address;
	}

	public String getIp()
	{
		return m_ip;
	}

	public int getPort()
	{
		return m_port;
	}

    public void setTag(Object tag)
    {
        m_tag = tag;
    }

    public Object getTag()
    {
        return m_tag;
    }

    /**
	 * @return the NIOService this responder is connected to.
	 */
	protected NIOService getNIOService()
	{
		return m_service;
	}

	/**
	 * Mark the observer for this responder as set.
	 * <p>
	 * This does not mean that the observer is really set, but instead that
	 * a set has been queued on the NIOService thread.
	 * <p>
	 * This call prevents the observer from being set more than once.
	 */
	protected void markObserverSet()
	{
		synchronized (this)
		{
			if (m_observerSet) throw new IllegalStateException("Listener already set.");
			m_observerSet = true;
		}
	}

	/**
	 * Called by the NIOService when the key has read interest ready.
	 */
	void socketReadyForRead()
    {
		throw new UnsupportedOperationException(getClass() + " does not support read.");
	}

	/**
	 * Called by the NIOService when the key has write interest ready.
	 */
	void socketReadyForWrite()
	{
		throw new UnsupportedOperationException(getClass() + " does not support write.");
	}

	/**
	 * Called by the NIOService when the key has accept interest ready.
	 */
	void socketReadyForAccept()
	{
		throw new UnsupportedOperationException(getClass() + " does not support accept.");
	}

	/**
	 * Called by the NIOService when the key has connect interest ready.
	 */
	void socketReadyForConnect()
	{
		throw new UnsupportedOperationException(getClass() + " does not support connect.");
	}

	/**
	 * Get the channel used by this responder.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the channel used by the responder.
	 */
	protected SelectableChannel getChannel()
	{
		return m_channel;
	}

	/**
	 * Sets the selection key of this responder.
	 * <p>
	 * This method is called on the NIOService-thread.
	 *
	 * @param key the selection key for this responder.
	 * @throws IllegalStateException if the selection key already is set.
	 */
	void setKey(SelectionKey key)
	{
		if (m_key != null) throw new IllegalStateException("Tried to set selection key twice");
		m_key = key;
		if (!isOpen())
		{
			// If we closed this before receiving the key, we should cancel the key.
			NIOUtils.cancelKeySilently(m_key);
			return;
		}
		keyInitialized();
		synchronizeKeyInterestOps();
	}

	/**
	 * Returns the selection key or null if the key is not set.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the selection key or nul if the responder is not yet registered.
	 */
	protected SelectionKey getKey()
	{
		return m_key;
	}

	/**
	 * This method is called after the SelectionKey is set.
	 * <p>
	 * This method is called on the NIOService-thread.
	 */
	abstract void keyInitialized();

	public boolean isOpen()
	{
		return m_open;
	}

	public void close()
	{
		close(null);
	}

	/**
	 * Asynchronously closes the channel with a given exception as a cause.
	 *
	 * @param exception the exception that caused the close, or null if this was
	 * closed by the user of this responder.
	 */
	protected void close(Exception exception)
	{
		if (isOpen())
		{
			getNIOService().queue(new CloseEvent(this, exception));
		}
	}

	/**
	 * Synchronizes the desired interest ops with the key interests ops,
	 * <em>if</em> the key is initialized.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 */
	private void synchronizeKeyInterestOps()
	{
		if (m_key != null)
		{
			try
			{
				int oldOps = m_key.interestOps();
				if ((m_interestOps & SelectionKey.OP_CONNECT) != 0)
				{
					m_key.interestOps(SelectionKey.OP_CONNECT);
				}
				else
				{
					m_key.interestOps(m_interestOps);
				}
				if (m_key.interestOps() != oldOps)
				{
					m_service.wakeup();
				}
			}
			catch (CancelledKeyException e)
			{
				// Ignore these.
			}
		}
	}

	/**
	 * Deleted an interest on a key.
	 * <p>
	 * This method is called on the NIOService-thread.
	 *
	 * @param interest the interest to delete.
	 */
	protected void deleteInterest(int interest)
	{
		m_interestOps = m_interestOps & ~interest;
		synchronizeKeyInterestOps();
	}

	/**
	 * Add an interest to the key, or change the currently pending interest.
	 * <p>
	 * This method is called on the NIOService-thread.
	 *
	 * @param interest the interest to add.
	 */
	protected void addInterest(int interest)
	{
		m_interestOps |= interest;
		synchronizeKeyInterestOps();
	}


	/**
	 * Returns a string on the format [ip]:[port]
	 * <em>This method is thread-safe.</em>
	 *
	 * @return a string on the format IP:port.
	 */
	@Override
	public String toString()
	{
		return m_ip + ":" + m_port;
	}

	/**
	 * Called once when the responder goes from state open to closed.
	 * <p>
	 * This method is called on the NIOService thread.
	 *
	 * @param e the exception that caused the shutdown, may be null.
	 */
	protected abstract void shutdown(Exception e);


	/**
	 * A close event sent when close() is called.
	 * <p>
	 * When run this event sets m_open to false and silently cancels
	 * the key and closes the channel.
	 * <p>
	 * Finally the responder's shutdown method is called.
	 */
	private static class CloseEvent implements Runnable
	{
		private final ChannelResponder m_responder;
		private final Exception m_exception;

		private CloseEvent(ChannelResponder responder, Exception e)
		{
			m_responder = responder;
			m_exception = e;
		}

		public void run()
		{
			if (m_responder.isOpen())
			{
				m_responder.m_open = false;
				NIOUtils.closeKeyAndChannelSilently(m_responder.getKey(), m_responder.getChannel());
				m_responder.shutdown(m_exception);
			}
		}
	}




}
