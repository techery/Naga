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
	 * Creates a '\n' delimited reader with an unlimited max buffer size.
	 */
	public AsciiLinePacketReader()
	{
		super((byte) '\n');
	}

	/**
	 * Creates a '\n' delimited reader with the given max line length
	 * and default read buffer size.
	 * <p>
	 * Exceeding the line length will throw an IOException.
	 *
	 * @param maxLineLength maximum line length.
	 */
	public AsciiLinePacketReader(int maxLineLength)
	{
		super((byte) '\n', maxLineLength);
	}

}
