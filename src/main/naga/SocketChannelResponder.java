package naga;

import naga.packetreader.RawPacketReader;
import naga.packetwriter.RawPacketWriter;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Christoffer Lerno
 */
class SocketChannelResponder extends ChannelResponder implements NIOSocket
{
	private int m_bytesRead;
	private int m_bytesWritten;
	private int m_maxQueueSize;
	private long m_timeOpened;
	private final AtomicLong m_bytesInQueue;
	private ConcurrentLinkedQueue<Object> m_packetQueue;
	private PacketReader m_packetReader;
	private PacketWriter m_packetWriter;
	private volatile SocketObserver m_socketObserver;

	public SocketChannelResponder(NIOService service, SocketChannel socketChannel, InetSocketAddress address)
	{
		super(service, socketChannel, address);
		m_socketObserver = null;
		m_maxQueueSize = -1;
		m_timeOpened = -1;
		m_packetWriter = new RawPacketWriter();
		m_packetReader = new RawPacketReader();
		m_bytesInQueue = new AtomicLong(0L);
		m_packetQueue = new ConcurrentLinkedQueue<Object>();
	}

	void keyInitialized()
	{
		if (!isConnected())
		{
			addInterest(SelectionKey.OP_CONNECT);
		}
	}

	public void closeAfterWrite()
	{
        queue(new Runnable() {
            public void run()
            {
                m_packetQueue.clear();
                close(null);
            }
        });
	}

    public void queue(Runnable runnable)
    {
        m_packetQueue.offer(runnable);
        addInterest(SelectionKey.OP_WRITE);
    }

	public boolean write(byte[] packet)
	{
		if (packet.length == 0) return true;
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

	public boolean isConnected()
	{
		return getChannel().isConnected();
	}

	public void socketReadyForRead()
	{
		if (!isOpen()) return;
		try
		{
			if (!isConnected()) throw new IOException("Channel not connected.");
			int read;
			while ((read = getChannel().read(m_packetReader.getBuffer())) > 0)
			{
				m_bytesRead += read;
				byte[] packet;
				while ((packet = m_packetReader.getNextPacket()) != null)
				{
					m_socketObserver.packetReceived(this, packet);
				}
			}
			if (read < 0)
			{
				throw new EOFException("Buffer read -1");
			}
		}
		catch (Exception e)
		{
			close(e);
		}
	}

	private void fillCurrentOutgoingBuffer() throws IOException
	{

		if (m_packetWriter.isEmpty())
		{
			// Retrieve next packet from the queue.
			Object nextPacket = m_packetQueue.poll();
            while (nextPacket != null && nextPacket instanceof Runnable)
            {
                ((Runnable) nextPacket).run();
                nextPacket = m_packetQueue.poll();
            }
            if (nextPacket == null) return;
            byte[] data = (byte[]) nextPacket;
            m_packetWriter.setPacket(data);
            // Remove the space reserved in the queue.
            m_bytesInQueue.addAndGet(-data.length);
		}
	}

	public void socketReadyForWrite()
	{
		try
		{
			deleteInterest(SelectionKey.OP_WRITE);
			if (!isOpen()) return;
			fillCurrentOutgoingBuffer();

			// Return if there is nothing in the buffer to send.
			if (m_packetWriter.isEmpty()) return;

			while (!m_packetWriter.isEmpty())
			{
				int written = getChannel().write(m_packetWriter.getBuffer());
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
		catch (Exception e)
		{
			close(e);
		}
	}
	
	public void socketReadyForConnect()
	{
		try
		{
			if (!isOpen()) return;
			if (getChannel().finishConnect())
			{
				deleteInterest(SelectionKey.OP_CONNECT);
				m_timeOpened = System.currentTimeMillis();
				notifyObserverOfConnect();
			}

		}
		catch (Exception e)
		{
			close(e);
		}
	}

	public void notifyWasCancelled()
	{
		close();
	}

	public Socket getSocket()
	{
		return getChannel().socket();
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
		markObserverSet();
		getNIOService().queue(new BeginListenEvent(this, socketObserver == null ? SocketObserver.NULL : socketObserver));
	}

	/**
	 * Notify the observer of our connect,
	 * swallowing exceptions thrown and logging them to stderr.
	 */
	@SuppressWarnings({"CallToPrintStackTrace"})
	private void notifyObserverOfConnect()
	{
		if (m_socketObserver == null) return;
		try
		{
			m_socketObserver.connectionOpened(this);
		}
		catch (Exception e)
		{
			// We have no way of properly logging this, which is why we log it to stderr
			e.printStackTrace();
		}
	}

	/**
	 * Notify the observer of our disconnect,
	 * swallowing exceptions thrown and logging them to stderr.
	 *
	 * @param exception the exception causing the disconnect, or null if this was a clean close.
	 */
	@SuppressWarnings({"CallToPrintStackTrace"})
	private void notifyObserverOfDisconnect(Exception exception)
	{
		if (m_socketObserver == null) return;
		try
		{
			m_socketObserver.connectionBroken(this, exception);
		}
		catch (Exception e)
		{
			// We have no way of properly logging this, which is why we log it to stderr
			e.printStackTrace();
		}
	}

	public void setPacketReader(PacketReader packetReader)
	{
		m_packetReader = packetReader;
	}

	public void setPacketWriter(final PacketWriter packetWriter)
	{
        if (packetWriter == null) throw new NullPointerException();
        queue(new Runnable() {
            public void run()
            {
                m_packetWriter = packetWriter;
            }
        });
 	}

	public SocketChannel getChannel()
	{
		return (SocketChannel) super.getChannel();
	}

	protected void shutdown(Exception e)
	{
		m_timeOpened = -1;
		m_packetQueue.clear();
		m_bytesInQueue.set(0);
		notifyObserverOfDisconnect(e);
	}


	private class BeginListenEvent implements Runnable
	{
		private final SocketObserver m_newObserver;
		private final SocketChannelResponder m_responder;

		private BeginListenEvent(SocketChannelResponder responder, SocketObserver socketObserver)
		{
			m_responder = responder;
			m_newObserver = socketObserver;
		}

		public void run()
		{
			m_responder.m_socketObserver =  m_newObserver;
			if (m_responder.isConnected())
			{
				m_responder.notifyObserverOfConnect();
			}
			if (!m_responder.isOpen())
			{
				m_responder.notifyObserverOfDisconnect(null);
			}
			m_responder.addInterest(SelectionKey.OP_READ);
		}

		@Override
		public String toString()
		{
			return "BeginListen[" + m_newObserver + "]";
		}
	}

	public Socket socket()
	{
		return getChannel().socket();
	}
}
