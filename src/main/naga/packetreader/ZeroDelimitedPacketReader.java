package naga.packetreader;

/**
 * Reads a bytestream delimited by 0.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class ZeroDelimitedPacketReader extends DelimiterPacketReader
{
	/**
	 * Creates zero delimited reader with a default read buffer
	 * and unlimited max packet size.
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
	 * @param readBufferSize the size of the read buffer (i.e. how many
	 * bytes are read in a single pass) - this only has effect on read
	 * efficiency and memory requirements.
	 * @param maxPacketSize the maximum packet size to accept.
	 */
	public ZeroDelimitedPacketReader(int readBufferSize, int maxPacketSize)
	{
		super((byte)0, readBufferSize, maxPacketSize);
	}

	/**
	 * Creates a zero delimited reader with the given max packet size
	 * and the default read buffer size.
	 * <p>
	 * Exceeding the packet size will throw a ProtocolViolationException.
	 *
	 * @param maxPacketSize the maximum packet size to accept.
	 */
	public ZeroDelimitedPacketReader(int maxPacketSize)
	{
		super((byte) 0, DEFAULT_READ_BUFFER_SIZE, maxPacketSize);
	}
}
