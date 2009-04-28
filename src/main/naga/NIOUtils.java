package naga;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

/**
 * A collection of utilites used by various classes.
 *
 * @author Christoffer Lerno
 */
public class NIOUtils
{

	NIOUtils() {}
	
	/**
	 * Silently close both a key and a channel.
	 *
	 * @param key the key to cancel, may be null.
	 * @param channel the channel to close, may be null.
	 */
	public static void closeKeyAndChannelSilently(SelectionKey key, Channel channel)
	{
		closeChannelSilently(channel);
		cancelKeySilently(key);
	}

	/**
	 * Creates a byte buffer with a given length with an encoded value,
	 * in either big or little endian encoding (i.e. biggest or smallest byte first).
	 *
	 * @param headerSize the header size in bytes. 1-4.
	 * @param valueToEncode the value to encode, 0 <= value < 2^(headerSize * 8)
	 * @param bigEndian if the encoding is big endian or not.
	 * @return a byte buffer with the number encoded.
	 * @throws IllegalArgumentException if the value is out of range for the given header size.
	 */
	public static ByteBuffer getByteBufferFromPacketSize(int headerSize, int valueToEncode, boolean bigEndian)
	{
		if (valueToEncode < 0) throw new IllegalArgumentException("Payload size is less than 0.");
		if (valueToEncode >> (headerSize * 8) > 0)
			throw new IllegalArgumentException("Payload size cannot be encoded into " + headerSize + " byte(s).");
		ByteBuffer header = ByteBuffer.allocate(headerSize);
		for (int i = 0; i < headerSize; i++)
		{
			int index = bigEndian ? (headerSize - 1 - i) : i;
			int value = (valueToEncode >> (8 * index)) % 256;
			header.put((byte) value);
		}
		header.rewind();
		return header;
	}

	/**
	 * Converts a value in a header buffer encoded in either big or little endian
	 * encoding.
	 * <p>
	 * <em>Note that trying to decode a value larger than 2^31 - 2 is not supported.</em>
	 *
	 * @param header the header to encode from.
	 * @param bigEndian if the encoding is big endian or not.
	 * @return the decoded number.
	 */
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

	/**
	 * Silently close a channel.
	 *
	 * @param channel the channel to close, may be null.
	 */
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

	/**
	 * Silently cancel a key.
	 *
	 * @param key the key to cancel, may be null.
	 */
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

}