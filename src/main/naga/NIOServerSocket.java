/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package naga;

import java.net.ServerSocket;

/**
 * Interface for the NIOServerSocket, which is
 * an asynchronous facade to an underlying ServerSocket.
 * <p>
 * The NIOServerSocket executes callbacks to a ServerSocket observer
 * to react to new connections and other events.
 *
 * @author Christoffer Lerno
 */
public interface NIOServerSocket extends NIOAbstractSocket
{

	/**
	 * Returns the total number of connections made on this socket since
	 * it opened.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the total number of connections made on this server socket.
	 */
	long getTotalConnections();

	/**
	 * Returns the total number of refused connections on this socket since
	 * it opened.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the total number of refused connections on this server socket.
	 */
	long getTotalRefusedConnections();

	/**
	 * Returns the total number of accepted connections on this socket since
	 * it opened.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the total number of accepted connections on this server socket.
	 */
	long getTotalAcceptedConnections();

	/**
	 * Returns the total number of failed connections on this socket since
	 * it opened.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the total number of failed connections on this server socket.
	 */
	long getTotalFailedConnections();

	/**
	 * Associates a server socket observer with this server socket and
	 * starts accepting connections.
	 * <p>
	 * <em>This method is thread-safe, but may only be called once.</em>
	 *
	 * @param observer the observer to receive callbacks from this socket.
	 * @throws NullPointerException if the observer given is null.
	 * @throws IllegalStateException if an observer has already been set.
	 */
	void listen(ServerSocketObserver observer);

	/**
	 * Sets the connection acceptor for this server socket.
	 * <p>
	 * A connection acceptor determines if a connection should be
	 * disconnected or not <em>after</em> the initial accept is done.
	 * <p>
	 * For more information, see the documentation for <code>naga.ConnectionAcceptor</code>.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param acceptor the acceptor to use, or null to default to
	 * ConnectorAcceptor.DENY.
	 */
	void setConnectionAcceptor(ConnectionAcceptor acceptor);

	/**
	 * Allows access to the underlying server socket.
	 * <p>
	 * <em>Note calling close and similar functions on this socket
	 * will put the NIOServerSocket in an undefined state</em>
	 *
	 * @return return the underlying server socket.
	 */
	ServerSocket socket();
}
