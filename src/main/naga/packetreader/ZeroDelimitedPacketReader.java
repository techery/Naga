package naga.packetreader;

/**
 * Reads a bytestream delimited by 0.
 *
 * @author Christoffer Lerno
 */
public class ZeroDelimitedPacketReader extends DelimiterPacketReader
{
	/**
	 * Creates zero delimited reader with an unlimited max packet size.
	 */
	public ZeroDelimitedPacketReader()
	{
		super((byte) 0);
	}

	/**
	 * Creates a zero delimited reader with the given max packet size
	 * and read buffer size.
	 * <p>
	 * Exceeding the packet size will throw a ProtocolViolationException.
	 *
	 * @param maxPacketSize the maximum packet size to accept.
	 */
	public ZeroDelimitedPacketReader(int maxPacketSize)
	{
		super((byte)0, maxPacketSize);
	}

}
