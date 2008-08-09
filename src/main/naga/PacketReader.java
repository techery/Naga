package naga;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface PacketReader
{
	ByteBuffer getBuffer();

	byte[] getNextPacket();
}
