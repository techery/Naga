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
	 * Creates a zero delimited reader with the given buffer size.
	 *
	 * @param bufferSize the buffer size to use.
	 */
	public ZeroDelimitedPacketReader(int bufferSize)
	{
		super(bufferSize, (byte) 0);
	}

	/**
	 * Creates a zero delimited packet reader with the default buffer size.
	 */
	public ZeroDelimitedPacketReader()
	{
		super((byte) 0);
	}
}
