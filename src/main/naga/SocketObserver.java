package naga;

/**
 * This interface contains the callbacks used by a NIOSocket
 * to inform its observer of events.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface SocketObserver
{
	/** A null object used as the default observer */
	SocketObserver NULL = new SocketObserverAdapter();

	/**
	 * Sent when a connection completes on a socket.
	 *
	 * @param nioSocket the socket that completed its connect.
	 */
	void connectionOpened(NIOSocket nioSocket);

	/**
	 * Sent when a connection is disconnected.
	 * <p>
	 * This may be sent even if a <code>connectionOpened(NIOSocket)</code>
	 * wasn't ever called, since the connect itself may
	 * fail.
	 *
	 * @param nioSocket the socket that was disconnected.
	 * @param exception the exception that caused the connection to break, may be null.
	 */
	void connectionBroken(NIOSocket nioSocket, Exception exception);

	/**
	 * Called when a packet is finished reading and contains
	 * the packet as parsed by the current PacketReader.
	 *
	 * @param socket the socket we received a packet on.
	 * @param packet the packet we received.
	 */
	void packetReceived(NIOSocket socket, byte[] packet);
}
