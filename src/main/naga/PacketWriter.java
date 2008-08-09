package naga;

import java.nio.ByteBuffer;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface PacketWriter
{
	void setPacket(byte[] bytes);

	boolean isEmpty();

	ByteBuffer getBuffer();
}
