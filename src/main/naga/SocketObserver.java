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

/**
 * This interface contains the callbacks used by a NIOSocket
 * to inform its observer of events.
 * <p>
 * All callbacks will be run on the NIOService-thread,
 * so callbacks should try to return as quickly as possible
 * since the callback blocks communication on all sockets
 * of the service.
 *
 * @author Christoffer Lerno
 */
public interface SocketObserver
{
	/** A null object used as the default observer */
	SocketObserver NULL = new SocketObserverAdapter();

	/**
	 * Called by the NIOService on the NIO thread when a connection completes on a socket.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param nioSocket the socket that completed its connect.
	 */
	void connectionOpened(NIOSocket nioSocket);

	/**
	 * Called by the NIOService on the NIO thread when a connection is disconnected.
	 * <p>
	 * This may be sent even if a <code>connectionOpened(NIOSocket)</code>
	 * wasn't ever called, since the connect itself may
	 * fail.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param nioSocket the socket that was disconnected.
	 * @param exception the exception that caused the connection to break, may be null.
	 */
	void connectionBroken(NIOSocket nioSocket, Exception exception);

	/**
	 * Called by the NIOService on the NIO thread when a packet is finished reading.
	 * The byte array contains the packet as parsed by the current PacketReader.
	 * <p>
	 * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
	 * all other connections until the method returns. It is therefore strongly recommended
	 * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
	 *
	 * @param socket the socket we received a packet on.
	 * @param packet the packet we received.
	 */
	void packetReceived(NIOSocket socket, byte[] packet);

    /**
     * Called by the NIOService on the NIO thread when a packet has finished writing.
     * <p>
     * <b>Note: Since this is a direct callback on the NIO thread, this method will suspend IO on
     * all other connections until the method returns. It is therefore strongly recommended
     * that the implementation of this method returns as quickly as possible to avoid blocking IO.</b>
     *
     * @param socket the socket we sent the packet on.
     * @param tag the (optional) tag associated with the packet.
     */
    void packetSent(NIOSocket socket, Object tag);
}
