package naga;

import javax.net.ssl.SSLEngine;

/**
 * Interface for a SSL Socket
 *
 * @author Christoffer Lerno
 */
public interface NIOSocketSSL extends NIOSocket
{
    /**
     * Returns the SSLEngine in use for this socket.
     *
     * @return an SSLEngine.
     */
    SSLEngine getSSLEngine();
}
