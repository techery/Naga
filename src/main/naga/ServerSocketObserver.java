package naga;

import java.io.IOException;

/**
 * Implemented by an observer to a server socket.
 * <p>
 * A server socket observer is responsible for handling
 * incoming connections by implementing a callback
 * to properly assign a SocketObserver to the new connections.
 * <p>
 * All callbacks will be run on the NIOService-thread,
 * so callbacks should try to return as quickly as possible
 * since the callback blocks communication on all sockets
 * of the service.
 * 
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface ServerSocketObserver
{
	/**
	 * Sent when an accept fails on the socket.
	 *
	 * @param exception the reason for the failure, never null.
	 */
	void acceptFailed(IOException exception);

	/**
	 * Sent when the server socket is closed.
	 *
	 * @param exception the exception that caused the close, or null if this was
	 * caused by an explicit <code>close()</code> on the NIOServerSocket.
	 */
	void serverSocketDied(Exception exception);

	/**
	 * Sent when a new connection has been accepted by the socket.
	 * <p>
	 * The normal behaviour would be for the observer to assign a reader and a writer to the socket,
	 * and then finally invoke <code>NIOSocket#listen(SocketObserver)</code> on the socket.
	 *
	 * @param nioSocket the socket that was accepted. 
	 */
	void newConnection(NIOSocket nioSocket);
	
}
