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

import java.net.InetSocketAddress;

/**
 * An interface describing methods common to both NIOSocket and NIOServerSocket.
 * 
 * @author Christoffer Lerno
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

    /**
     * Returns the tag for this socket.
     *
     * @return the tag or null if no tag has been set.
     */
    Object getTag();

    /**
     * Returns the tag for this socket. A tag is an object
     * that you can associate with the socket and retrieve later.
     *
     * @param tag the new tag for this socket.
     */
    void setTag(Object tag);
}
