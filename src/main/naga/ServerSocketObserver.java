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
 */
public interface ServerSocketObserver
{
	/**
	 * Called by the NIOService on the NIO thread when an accept fails on the socket.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param exception the reason for the failure, never null.
	 */
	void acceptFailed(IOException exception);

	/**
	 * Called by the NIOService on the NIO thread when the server socket is closed.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param exception the exception that caused the close, or null if this was
	 * caused by an explicit <code>close()</code> on the NIOServerSocket.
	 */
	void serverSocketDied(Exception exception);

	/**
	 * Called by the NIOService on the NIO thread when a new connection has been accepted by the socket.
	 * <p>
	 * The normal behaviour would be for the observer to assign a reader and a writer to the socket,
	 * and then finally invoke <code>NIOSocket#listen(SocketObserver)</code> on the socket.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param nioSocket the socket that was accepted. 
	 */
	void newConnection(NIOSocket nioSocket);
	
}
