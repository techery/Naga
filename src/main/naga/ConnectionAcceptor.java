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
 * The ConnectionAcceptor is used by the NIOServerSocket to determine
 * if a connection should be accepted or refused.
 * <p>
 * This can be used to implement black-listing of certain IP-ranges
 * or to limit the number of simultaneous connection. However,
 * in most cases it is enough to use the ConnectorAcceptor.ALLOW which
 * accepts all incoming connections.
 * <p>
 * Note that a NIOServerSocket defaults to the ConnectorAcceptor.ALLOW 
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
