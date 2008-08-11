package naga;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOServerSocket
{
	void close();

	long getTotalConnections();

	long getTotalRefusedConnections();

	long getTotalAcceptedConnections();

	long getTotalFailedConnections();

	boolean isOpen();

	String getLocalAddress();

	int getLocalPort();

	NIOService getService();

	void listen(ServerSocketObserver observer);

	void setConnectionAcceptor(ConnectionAcceptor acceptor);
}
