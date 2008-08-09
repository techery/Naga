package naga;

import naga.SocketObserver;
import naga.PacketWriter;
import naga.PacketReader;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOSocket
{
	boolean write(byte[] packet);

	String getIp();

	int getLocalPort();

	/**
	 * @return the total number of bytes read on this socket.
	 */
	long getBytesRead();

	/**
	 * @return the total number of bytes written on this socket.
	 */
	long getBytesWritten();

	/**
	 * @return the time this socket has been open in ms.
	 */
	long getTimeOpen();

	/**
	 * @return the number of bytes waiting to be written in the underlying queue. This excludes the
	 * amount of bytes in the currently writing packet.
	 */
	long getWriteQueueSize();

	boolean isWriting();

	/**
	 * @return the current maximum queue size.
	 */
	int getMaxQueueSize();

	/**
	 * Sets the maximum number of bytes allowed in the queue for this socket. If this
	 * number is less than 1, the queue is unbounded.
	 *
	 * @param maxQueueSize the new max queue size. A value less than 1 is an unbounded queue.
	 */
	void setMaxQueueSize(int maxQueueSize);

	/**
	 * Sets the packet reader for this socket.
	 *
	 * @param packetReader the packet reader to interpret the incoming byte stream.
	 */
	void setPacketReader(PacketReader packetReader);

	/**
	 * Sets the packet writer for this socket.
	 *
	 * @param packetWriter the packet writer to interpret the incoming byte stream.
	 */
	void setPacketWriter(PacketWriter packetWriter);

	/**
	 * Sets the receiver of packets for this socket.
	 *
	 * @param socketObserver the observer to receive packets and be notified of disconnects.
	 */
	void setObserver(SocketObserver socketObserver);

	boolean isAlive();

	/**
	 * Causes the socket to close after writing the current entries in the queue
	 * (consequent entries will be thrown away)
	 */
	void closeAfterWrite();

	void close();
}
