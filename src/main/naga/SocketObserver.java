package naga;

/**
 * This interface contains the callbacks used by a NIOSocket
 * to inform its observer of events.
 * <p>
 * All callbacks will be run on the NIOService-thread,
 * so callbacks should try to return as quickly as possible
 * since the callback blocks communication on all sockets
 * of the service.
 *
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface SocketObserver
{
	/** A null object used as the default observer */
	SocketObserver NULL = new SocketObserverAdapter();

	/**
	 * Called by the NIOService on the NIO thread when a connection completes on a socket.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param nioSocket the socket that completed its connect.
	 */
	void connectionOpened(NIOSocket nioSocket);

	/**
	 * Called by the NIOService on the NIO thread when a connection is disconnected.
	 * <p>
	 * This may be sent even if a <code>connectionOpened(NIOSocket)</code>
	 * wasn't ever called, since the connect itself may
	 * fail.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param nioSocket the socket that was disconnected.
	 * @param exception the exception that caused the connection to break, may be null.
	 */
	void connectionBroken(NIOSocket nioSocket, Exception exception);

	/**
	 * Called by the NIOService on the NIO thread when a packet is finished reading.
	 * The byte array contains the packet as parsed by the current PacketReader.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param socket the socket we received a packet on.
	 * @param packet the packet we received.
	 */
	void packetReceived(NIOSocket socket, byte[] packet);
}
