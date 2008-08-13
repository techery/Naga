package naga.packetreader;

/**
 * Reads a bytestream delimited by '\n'.
 * <p>
 * This can be used for reading lines of ASCII characters.
 * 
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class AsciiLinePacketReader extends DelimiterPacketReader
{
	/**
	 * Creates a '\n' delimited reader with default buffer size.
	 */
	public AsciiLinePacketReader()
	{
		super((byte) '\n');
	}

	/**
	 * Creates a '\n' delimited reader with the given buffer size.
	 *
	 * @param bufferSize buffer size for the byte buffer reading lines.
	 */
	public AsciiLinePacketReader(int bufferSize)
	{
		super(bufferSize, (byte) '\n');
	}
}
