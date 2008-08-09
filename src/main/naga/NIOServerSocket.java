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

	boolean isAlive();

	String getLocalAddress();

	int getLocalPort();

	NIOService getService();

	void setObserver(ServerSocketObserver observer);

	void setConnectionAcceptor(ConnectionAcceptor acceptor);
}
