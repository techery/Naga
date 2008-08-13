package naga;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOServerSocket extends NIOAbstractSocket
{

	/**
	 * Returns the total number of connections made on this socket since
	 * it opened.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @return the total number of connections made on this server socket.
	 */
	long getTotalConnections();

	/**
	 * Returns the total number of refused connections on this socket since
	 * it opened.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @return the total number of refused connections on this server socket.
	 */
	long getTotalRefusedConnections();

	/**
	 * Returns the total number of accepted connections on this socket since
	 * it opened.
	 * <p>
	 * This method is thread-safe.
	 * @return the total number of accepted connections on this server socket.
	 */
	long getTotalAcceptedConnections();

	/**
	 * Returns the total number of failed connections on this socket since
	 * it opened.
	 * <p>
	 * This method is thread-safe.
	 *
	 * @return the total number of failed connections on this server socket.
	 */
	long getTotalFailedConnections();

	/**
	 * Associates a server socket observer with this server socket and
	 * starts accepting connections.
	 * <p>
	 * This method is thread-safe, but may only be called once.
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
	 * See <code>ConnectionAcceptor</code>
	 * <p>
	 * This method is thread-safe.
	 *
	 * @param acceptor the acceptor to use, or null to default to
	 * ConnectorAcceptor.DENY.
	 */
	void setConnectionAcceptor(ConnectionAcceptor acceptor);
}
