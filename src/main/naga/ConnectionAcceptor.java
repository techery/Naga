package naga;

import java.net.InetSocketAddress;

/**
 * The ConnectionAcceptor is used by the NIOServerSocket to determine
 * if a connection should be accepted or refused.
 * <p>
 * This can be used to implement black-listing of certain IP-ranges
 * or to limit the number of simultaneous connection. However,
 * in most cases it is enough to use the ConnectorAcceptor.ALLOW which
 * accepts all incoming connections.
 * <p>
 * Note that a NIOServerSocket defaults to the ConnectorAcceptor.DENY
 * acceptor when it is created.
 *
 *
 * @author Christoffer Lerno
 */
public interface ConnectionAcceptor
{
	/**
	 * A connection acceptor that refuses all connections.
	 */
	ConnectionAcceptor DENY = new ConnectionAcceptor()
	{
		public boolean acceptConnection(InetSocketAddress address)
		{
			return false;
		}
	};

	/**
	 * A connection acceptor that accepts all connections.
	 */
	ConnectionAcceptor ALLOW = new ConnectionAcceptor()
	{
		public boolean acceptConnection(InetSocketAddress address)
		{
			return true;
		}
	};

	/**
	 * Return true if the connection should be accepted, false otherwise.
	 * <p>
	 * <b>Note: This callback is run on the NIOService thread. This means it will block
	 * <u>all</u> other reads, writes and accepts on the service while it executes.
	 * For this reason it is recommended that this method should return fairly quickly
	 * (i.e. don't make reverse ip lookups or similar - potentially very slow - calls).
	 * </b>
	 * 
	 * @param inetSocketAddress the adress the connection came from.
	 * @return true to accept, false to refuse.
	 */
	boolean acceptConnection(InetSocketAddress inetSocketAddress);
}
