package naga;

import naga.packetwriter.RawPacketWriter;
import naga.packetreader.RawPacketReader;

import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class SocketChannelResponder implements ChannelResponder, NIOSocket
{
	private final static byte[] CLOSE_PACKET = new byte[0];
	private final SocketChannel m_channel;
	private final SelectionKey m_key;
	private boolean m_alive;
	private int m_bytesRead;
	private int m_bytesWritten;
	private int m_maxQueueSize;
	private long m_timeOpened;
	private final AtomicLong m_bytesInQueue;
	private ConcurrentLinkedQueue<byte[]> m_packetQueue;
	private PacketReader m_packetReader;
	private PacketWriter m_packetWriter;
	private SocketObserver m_socketObserver;

	public SocketChannelResponder(SocketChannel socketChannel, SelectionKey key)
	{
		m_channel = socketChannel;
		m_key = key;
		setObserver(null);
		m_alive = true;
		m_maxQueueSize = -1;
		m_timeOpened = -1;
		m_packetWriter = new RawPacketWriter();
		m_packetReader = new RawPacketReader(16);
		m_bytesInQueue = new AtomicLong(0L);
		m_packetQueue = new ConcurrentLinkedQueue<byte[]>();
		if (socketChannel.isConnected())
		{
			key.interestOps(SelectionKey.OP_READ);
		}
		else
		{
			key.interestOps(SelectionKey.OP_CONNECT);
		}
	}

	public void closeAfterWrite()
	{
		// Add a null packet signaling close.
		m_packetQueue.offer(CLOSE_PACKET);

		// Make sure the interests are set.
		m_key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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

		// Make sure the interests are set.
		m_key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		return true;
	}

	public void notifyCanRead()
	{
		if (!m_alive) return;
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
		catch (IOException e)
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
			m_key.interestOps(SelectionKey.OP_READ);
			if (m_alive)
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
						m_key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						return;
					}
					if (m_packetWriter.isEmpty())
					{
						fillCurrentOutgoingBuffer();
					}
				}
			}
		}
		catch (IOException e)
		{
			close();
		}
	}

	public void notifyCanAccept()
	{
		throw new UnsupportedOperationException("Operation not supported for regular sockets");
	}

	public void notifyCanConnect()
	{
		try
		{
			if (m_alive)
			{
				if (m_channel.finishConnect())
				{
					m_key.interestOps(SelectionKey.OP_READ);
					m_socketObserver.notifyConnect(this);
					m_timeOpened = System.currentTimeMillis();
				}
			}
		}
		catch (IOException e)
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
		if (m_alive)
		{
			m_timeOpened = -1;
			m_alive = false;
			m_socketObserver.notifyDisconnect(this);
			m_packetQueue.clear();
			m_bytesInQueue.set(0);
			NIOUtils.closeKeyAndChannelSilently(m_key, m_channel);
		}
	}

	public String getIp()
	{
		if (m_alive)
		{
			try
			{
				return m_channel.socket().getInetAddress().getHostAddress();
			}
			catch (Exception e)
			{
				// Do nothing, return the default -1
			}
		}
		return "0.0.0.0";
	}
	public int getLocalPort()
	{
		if (m_alive)
		{
			try
			{
				return m_channel.socket().getLocalPort();
			}
			catch (Exception e)
			{
				// Do nothing, return the default -1
			}
		}
		return -1;
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
		return "" + getIp() + ":" + getLocalPort();
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

	public void setObserver(SocketObserver socketObserver)
	{
		m_socketObserver = socketObserver == null ? SocketObserver.NULL : socketObserver;
	}

	public boolean isAlive()
	{
		return m_alive;
	}

	public void setPacketReader(PacketReader packetReader)
	{
		m_packetReader = packetReader;
	}

	public void setPacketWriter(PacketWriter packetWriter)
	{
		m_packetWriter = packetWriter;
	}

}
