package naga;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOPacket
{
	ByteBuffer getNextBuffer();

	int size();

	boolean isComplete();
}
