package naga;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface SocketObserver
{
	SocketObserver NULL = new SocketObserverAdapter();

	void notifyConnect(NIOSocket nioSocket);

	void notifyDisconnect(NIOSocket nioSocket);

	void notifyReadPacket(NIOSocket socket, byte[] packet);
}
