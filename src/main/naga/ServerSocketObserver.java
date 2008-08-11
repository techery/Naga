package naga;

import java.io.IOException;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface ServerSocketObserver
{
	void notifyAcceptingConnectionFailed(IOException exception);

	void notifyServerSocketDied();

	void newConnection(NIOSocket nioSocket);
	
}
