package naga.packetreader;

/**
 * Reads a bytestream delimited by '\n'.
 * <p>
 * This can be used for reading lines of ASCII characters.
 * 
 * @author Christoffer Lerno
 */
public class AsciiLinePacketReader extends DelimiterPacketReader
{
	/**
	 * Creates a '\n' delimited reader with default min buffer size
	 * and unlimited max buffer size.
	 */
	public AsciiLinePacketReader()
	{
		super((byte) '\n');
	}

	/**
	 * Creates a '\n' delimited reader with the given max line length
	 * and read buffer size.
	 * <p>
	 * Exceeding the line length will throw a ProtocolViolationException.
	 *
	 * @param readBufferSize the size of the read buffer (i.e. how many
	 * bytes are read in a single pass) - this only has effect on read
	 * efficiency and memory requirements.
	 * @param maxLineLength maximum line length.
	 */
	public AsciiLinePacketReader(int readBufferSize, int maxLineLength)
	{
		super((byte) '\n', readBufferSize, maxLineLength);
	}

	/**
	 * Creates a '\n' delimited reader with the given max line length
	 * and default read buffer size.
	 * <p>
	 * Exceeding the line length will throw a ProtocolViolationException.
	 *
	 * @param maxLineLength maximum line length.
	 */
	public AsciiLinePacketReader(int maxLineLength)
	{
		super((byte) '\n', DEFAULT_READ_BUFFER_SIZE, maxLineLength);
	}

}
