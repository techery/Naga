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
public class SSLServerSocketChannelResponder extends ServerSocketChannelResponder implements NIOServerSocketSSL
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
        return new SSLSocketChannelResponder(socket,  m_sslContext.createSSLEngine(), false);
    }
}
