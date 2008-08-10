package naga;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class NIOUtils
{
	public final static SelectionKey NULL_KEY = new NullKey();

	public static void closeKeyAndChannelSilently(SelectionKey key, Channel channel)
	{
		closeChannelSilently(channel);
		cancelKeySilently(key);
	}

	public static ByteBuffer getByteBufferFromPacketSize(int headerSize, int size, boolean bigEndian)
	{
		if (size >> (headerSize * 8) > 0)
			throw new IllegalArgumentException("Payload size cannot be encoded into " + headerSize + " bytes.");
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		for (int i = 0; i < headerSize; i++)
		{
			int index = bigEndian ? (headerSize - 1 - i) : i;
			int value = (size >> (8 * index)) % 256;
			header.put((byte) value);
		}
		header.rewind();
		return header;
	}

	public static int getPacketSizeFromByteBuffer(ByteBuffer header, boolean bigEndian)
	{
		int packetSize = 0;
		if (bigEndian)
		{
			header.rewind();
			while (header.hasRemaining())
			{
				packetSize <<= 8;
				packetSize += header.get() & 0xFF;
			}
		}
		else
		{
			header.rewind();
			int multiple = 1;
			while (header.hasRemaining())
			{
				packetSize += multiple * (header.get() & 0xFF);
				multiple <<= 8;
			}
		}
		return packetSize;
	}

	public static void closeChannelSilently(Channel channel)
	{
		try
		{
			if (channel != null)
			{
				channel.close();
			}
		}
		catch (IOException e)
		{
			// Do nothing
		}
	}

	public static void cancelKeySilently(SelectionKey key)
	{
		try
		{
			if (key != null) key.cancel();
		}
		catch (Exception e)
		{
			// Do nothing
		}
	}

	private static class NullKey extends SelectionKey
	{
		public void cancel()
		{

		}

		public SelectableChannel channel()
		{
			return null;
		}

		public int interestOps()
		{
			return 0;
		}

		public SelectionKey interestOps(int ops)
		{
			return this;
		}

		public boolean isValid()
		{
			return false;
		}

		public int readyOps()
		{
			return 0;
		}

		public Selector selector()
		{
			return null;
		}
	}

}