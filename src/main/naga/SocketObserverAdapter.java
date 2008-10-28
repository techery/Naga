package naga;

/**
 * Class with null-implementation of all SocketObserver callbacks.
 * 
 * @author Christoffer Lerno
 */
public class SocketObserverAdapter implements SocketObserver
{

	public void connectionBroken(NIOSocket nioSocket, Exception exception)
	{
	}

	public void packetReceived(NIOSocket socket, byte[] packet)
	{

	}

	public void connectionOpened(NIOSocket nioSocket)
	{
	}
}
