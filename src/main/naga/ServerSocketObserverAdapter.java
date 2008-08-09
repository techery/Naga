package naga;

import java.io.IOException;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class ServerSocketObserverAdapter implements ServerSocketObserver
{
	public void notifyAcceptingConnectionFailed(IOException exception)
	{
	}

	public void notifyServerSocketDied()
	{
	}

	public void newConnection(NIOSocket nioSocket)
	{
	}
}
