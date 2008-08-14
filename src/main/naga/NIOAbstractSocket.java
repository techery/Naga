package naga;

import java.net.InetSocketAddress;

/**
 * An interface describing methods common to both NIOSocket and NIOServerSocket.
 * 
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface NIOAbstractSocket
{
	/**
	 * Closes this socket (the actual disconnect will occur on the NIOService thread)
	 * <p>
	 * <em>This method is thread-safe.</em>
	 */
	void close();

	/**
	 * Returns the InetSocketAddress for this socket.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the InetSocketAddress this socket connects to.
	 */
	InetSocketAddress getAddress();

	/**
	 * Returns the current state of this socket.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 * 
	 * @return true if the connection is socket is open, false if closed.
	 */
	boolean isOpen();

	/**
	 * Reports the IP used by this socket.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the IP of this socket.
	 */
	String getIp();

	/**
	 * Returns the port in use by this socket.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return the port of this socket.
	 */
	int getPort();
}
