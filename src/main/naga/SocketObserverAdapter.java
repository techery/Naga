package naga;

import naga.SocketObserver;
import naga.NIOSocket;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class SocketObserverAdapter implements SocketObserver
{

	public void notifyDisconnect(NIOSocket nioSocket)
	{
	}

	public void notifyReadPacket(NIOSocket socket, byte[] packet)
	{

	}

	public void notifyConnect(NIOSocket nioSocket)
	{
	}
}
