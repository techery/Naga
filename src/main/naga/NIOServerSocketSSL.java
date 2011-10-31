package naga;

import javax.net.ssl.SSLContext;

/**
 * Interface for SSL Server Sockets
 *
 * @author Christoffer Lerno
 */
public interface NIOServerSocketSSL extends NIOServerSocket
{
    /**
     * Returns the SSLContext in use.
     *
     * @return the SSLContext.
     */
    public SSLContext getSSLContext();

}
