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
