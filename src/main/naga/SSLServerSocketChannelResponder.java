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

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * This is a ServerSocketChannel subclass that starts SSL communication on all its sockets.
 *
 * @author Christoffer Lerno
 */
class SSLServerSocketChannelResponder extends ServerSocketChannelResponder implements NIOServerSocketSSL
{
    private final SSLContext m_sslContext;
    public SSLServerSocketChannelResponder(SSLContext context, NIOService service, ServerSocketChannel channel, InetSocketAddress address) throws IOException
    {
        super(service, channel, address);
        m_sslContext = context;
    }

    public SSLContext getSSLContext()
    {
        return m_sslContext;
    }

    @Override
    NIOSocket registerSocket(SocketChannel channel, InetSocketAddress address) throws IOException
    {
        NIOSocket socket = super.registerSocket(channel, address);
        return new SSLSocketChannelResponder(getNIOService(), socket,  m_sslContext.createSSLEngine(), false);
    }
}
