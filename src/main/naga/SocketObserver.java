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
	void notifyConnect(NIOSocket nioSocket);

	/**
	 * Sent when a connection is disconnected.
	 * <p>
	 * This may be sent even if a #notifyConnect
	 * wasn't ever called, since the connect itself may
	 * fail.
	 *
	 * @param nioSocket the socket that was disconnected.
	 */
	void notifyDisconnect(NIOSocket nioSocket);

	/**
	 * Called when a packet is finished reading and contains
	 * the packet as parsed by the current PacketReader.
	 *
	 * @param socket the socket we received a packet on.
	 * @param packet the packet we received.
	 */
	void notifyReadPacket(NIOSocket socket, byte[] packet);
}
