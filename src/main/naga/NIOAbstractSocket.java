package naga;

import java.net.InetSocketAddress;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOAbstractSocket
{
	/**
	 * Closes this socket (the actual disconnect will occur on the NIOService thread)
	 * <p>
	 * This method is thread-safe.
	 */
	void close();

	/**
	 * Returns the InetSocketAddress for this socket.
	 *
	 * @return the InetSocketAddress this socket connects to.
	 */
	InetSocketAddress getAddress();

	/**
	 * Returns the current state of this socket.
	 * <p>
	 * This method is thread-safe.
	 * 
	 * @return true if the connection is socket is open, false if closed.
	 */
	boolean isOpen();

	/**
	 * Reports the IP used by this socket.
	 *
	 * @return the IP of this socket.
	 */
	String getIp();

	/**
	 * Returns the port in use by this socket.
	 *
	 * @return the port of this socket.
	 */
	int getPort();
}
