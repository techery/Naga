package naga;

import naga.packetreader.RawPacketReader;
import naga.packetwriter.RawPacketWriter;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
class SocketChannelResponder implements ChannelResponder, NIOSocket
{
	private final static byte[] CLOSE_PACKET = new byte[0];
	private final SocketChannel m_channel;
	private SelectionKey m_key;
	private final AtomicBoolean m_open;
	private int m_bytesRead;
	private int m_bytesWritten;
	private int m_maxQueueSize;
	private long m_timeOpened;
	private final AtomicLong m_bytesInQueue;
	private ConcurrentLinkedQueue<byte[]> m_packetQueue;
	private PacketReader m_packetReader;
	private PacketWriter m_packetWriter;
	private SocketObserver m_socketObserver;
	private SocketObserver m_disconnectObserver;
	private SocketObserver m_connectObserver;
	private volatile int m_interestOps;

	public SocketChannelResponder(SocketChannel socketChannel)
	{
		m_channel = socketChannel;
		m_key = null;
		m_socketObserver = null;
		m_open = new AtomicBoolean(true);
		m_maxQueueSize = -1;
		m_timeOpened = -1;
		m_interestOps = 0;
		m_disconnectObserver = null;
		m_connectObserver = null;
		m_packetWriter = new RawPacketWriter();
		m_packetReader = new RawPacketReader();
		m_bytesInQueue = new AtomicLong(0L);
		m_packetQueue = new ConcurrentLinkedQueue<byte[]>();
	}

	/**
	 * Set the SelectionKey for this responder. Should be done by the NIOService.
	 * <p>
	 * Will switch to current interest opts.
	 *
	 * @param key the new key.
	 * @throws IllegalStateException if the selection key was already set.
	 */
	public synchronized void setKey(SelectionKey key)
	{
		if (m_key != null) throw new IllegalStateException("Tried to set selection key twice");
		m_key = key;
		if (!isOpen())
		{
			// If we closed this before receiving the key, we should cancel the key.
			NIOUtils.cancelKeySilently(key);
			return;
		}
		if (!m_channel.isConnected())
		{
			m_interestOps |= SelectionKey.OP_CONNECT;
		}
		synchronizeKeyInterestOps();
	}

	public synchronized void closeAfterWrite()
	{
		// Add a null packet signaling close.
		m_packetQueue.offer(CLOSE_PACKET);

		// Make sure the interests are set.
		addInterest(SelectionKey.OP_WRITE);
	}

	public boolean write(byte[] packet)
	{
		long currentQueueSize = m_bytesInQueue.addAndGet(packet.length);
		if (m_maxQueueSize > 0 && currentQueueSize > m_maxQueueSize)
		{
			m_bytesInQueue.addAndGet(-packet.length);
			return false;
		}

		// Add the packet.
		m_packetQueue.offer(packet);
		addInterest(SelectionKey.OP_WRITE);

		return true;
	}

	public void notifyCanRead()
	{
		if (!m_open.get()) return;
		try
		{
			int read = m_channel.read(m_packetReader.getBuffer());
			if (read < 0)
			{
				close();
			}
			if (read > 0)
			{
				m_bytesRead += read;
				byte[] packet;
				while ((packet = m_packetReader.getNextPacket()) != null)
				{
					m_socketObserver.notifyReadPacket(this, packet);
				}
			}
		}
		catch (Exception e)
		{
			close();
		}
	}

	private void fillCurrentOutgoingBuffer() throws IOException
	{

		if (m_packetWriter.isEmpty())
		{
			// Retrieve next packet from the queue.
			byte[] nextPacket = m_packetQueue.poll();

			if (nextPacket == CLOSE_PACKET) throw new IOException("CLOSE");

			if (nextPacket != null)
			{
				m_packetWriter.setPacket(nextPacket);
				// Remove the space reserved in the queue.
				m_bytesInQueue.addAndGet(-nextPacket.length);
			}
		}
	}

	public void notifyCanWrite()
	{
		try
		{
			deleteInterest(SelectionKey.OP_WRITE);
			if (isOpen())
			{
				fillCurrentOutgoingBuffer();

				// Return if there is nothing in the buffer to send.
				if (m_packetWriter.isEmpty()) return;

				while (!m_packetWriter.isEmpty())
				{
					int written = m_channel.write(m_packetWriter.getBuffer());
					m_bytesWritten += written;
					if (written == 0)
					{
						// Change the interest ops in case we still have things to write.
						addInterest(SelectionKey.OP_WRITE);
						return;
					}
					if (m_packetWriter.isEmpty())
					{
						fillCurrentOutgoingBuffer();
					}
				}
			}
		}
		catch (Exception e)
		{
			close();
		}
	}

	public void notifyCanAccept()
	{
		throw new UnsupportedOperationException("Operation not supported for regular sockets");
	}

	public String getIp()
	{
		return getSocket().getInetAddress().getHostAddress();
	}
	public void notifyCanConnect()
	{
		try
		{
			if (isOpen())
			{
				if (m_channel.finishConnect())
				{
					deleteInterest(SelectionKey.OP_CONNECT);
					notifyObserverOfConnect();
					m_timeOpened = System.currentTimeMillis();
				}
			}
		}
		catch (Exception e)
		{
			close();
		}
	}

	public void notifyWasCancelled()
	{
		close();
	}

	public void close()
	{
		if (m_open.compareAndSet(true, false))
		{
			m_timeOpened = -1;
			m_packetQueue.clear();
			m_bytesInQueue.set(0);
			NIOUtils.closeKeyAndChannelSilently(m_key, m_channel);
			notifyObserverOfDisconnect();
		}
	}

	public Socket getSocket()
	{
		return m_channel.socket();
	}

	public long getBytesRead()
	{
		return m_bytesRead;
	}

	public long getBytesWritten()
	{
		return m_bytesWritten;
	}

	public long getTimeOpen()
	{
		return m_timeOpened > 0 ? System.currentTimeMillis() - m_timeOpened : -1;
	}

	public long getWriteQueueSize()
	{
		return m_bytesInQueue.get();
	}

	public boolean isWriting()
	{
		return !m_packetWriter.isEmpty() || m_packetQueue.peek() != null;
	}

	public String toString()
	{
		try
		{
			return getSocket().toString();
		}
		catch (Exception e)
		{
			return "Closed NIO Socket";
		}
	}

	/**
	 * @return the current maximum queue size.
	 */
	public int getMaxQueueSize()
	{
		return m_maxQueueSize;
	}

	/**
	 * Sets the maximum number of bytes allowed in the queue for this socket. If this
	 * number is less than 1, the queue is unbounded.
	 *
	 * @param maxQueueSize the new max queue size. A value less than 1 is an unbounded queue.
	 */
	public void setMaxQueueSize(int maxQueueSize)
	{
		m_maxQueueSize = maxQueueSize;
	}

	public void listen(SocketObserver socketObserver)
	{
		// Synchronization ensures that listen is not called from two threads at the same time.
		synchronized (this)
		{
			if (m_socketObserver != null) throw new IllegalStateException("There is already an observer listening to this socket.");
			m_socketObserver = socketObserver == null ? SocketObserver.NULL : socketObserver;
		}
		if (m_channel.isConnected())
		{
			notifyObserverOfConnect();
		}
		if (!isOpen())
		{
			notifyObserverOfDisconnect();
		}
		addInterest(SelectionKey.OP_READ);
	}

	private void notifyObserverOfConnect()
	{
		synchronized (this)
		{
			if (m_connectObserver == m_socketObserver) return;
			m_connectObserver = m_socketObserver;
		}
		try
		{
			m_connectObserver.notifyConnect(this);
			System.out.println(this);
		}
		catch (Exception e)
		{
			// We have no way of properly logging this, which is why we log it to stderr
			e.printStackTrace();
		}
	}

	private void notifyObserverOfDisconnect()
	{
		synchronized (this)
		{
			if (m_disconnectObserver == m_socketObserver) return;
			m_disconnectObserver = m_socketObserver;
		}
		try
		{
			m_disconnectObserver.notifyDisconnect(this);
		}
		catch (Exception e)
		{
			// We have no way of properly logging this, which is why we log it to stderr
			e.printStackTrace();
		}
	}


	public boolean isOpen()
	{
		return m_open.get();
	}

	public void setPacketReader(PacketReader packetReader)
	{
		m_packetReader = packetReader;
	}

	public void setPacketWriter(PacketWriter packetWriter)
	{
		m_packetWriter = packetWriter;
	}

	public SelectableChannel getChannel()
	{
		return m_channel;
	}

	/**
	 * Add an interest to the key, or change the currently pending interest.
	 *
	 * @param interest the interest to add.
	 */
	private void addInterest(int interest)
	{
		m_interestOps |= interest;
		synchronizeKeyInterestOps();
	}

	/**
	 * Synchronizes the desired interest ops with the key interests ops,
	 * <em>if</em> the key is initialized.
	 */
	private void synchronizeKeyInterestOps()
	{
		if (m_key != null)
		{
			try
			{
				m_key.interestOps(m_interestOps);
			}
			catch (CancelledKeyException e)
			{
				// Ignore these.
			}
		}
	}

	/**
	 * Deleted an interest on a key.
	 *
	 * @param interest the interest to delete.
	 */
	private void deleteInterest(int interest)
	{
		m_interestOps = m_interestOps & ~interest;
		synchronizeKeyInterestOps();
	}
}
