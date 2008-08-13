package naga;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOSocket extends NIOAbstractSocket
{
	/**
	 * Write a packet of bytes asynchronously on this socket.
	 * <p>
	 * The bytes will be sent to the PacketWriter belonging to this
	 * socket for dispatch. However, if the queue is full (i.e. the new
	 * queue size would exceed <code>getMaxQueueSize()</code>),
	 * the packet is discarded and the method returns false.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param packet the packet to send.
	 * @return true if the packet was queued, false if the queue limit
	 * was reached and the packet was thrown away.
	 */
	boolean write(byte[] packet);

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
	 * This method returns the number of bytes queued for dispatch.
	 * This size is compared against the maximum queue size to determine if additional packets
	 * will be refused or not.
	 * <p>
	 * However, this number does not include the packet currently waiting to be written.
	 *
	 * @return the total size of the packets waiting to be dispatched, exluding the currently
	 * dispatching packet.
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
	 * Opens the socket for reads.
	 * <p>
	 * The socket observer will receive connects, disconnects and packets.
	 * If the socket was opened or disconnected before the observer was attached,
	 * the socket observer will still receive those callbacks.
	 *
	 * @param socketObserver the observer to receive packets and be notified of connects/disconnects.
	 */
	void listen(SocketObserver socketObserver);


	/**
	 * Causes the socket to close after writing the current entries in the queue
	 * (consequent entries will be thrown away).
	 */
	void closeAfterWrite();
}
