package naga;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

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

    /**
     * Initiates SSL-handshake, starts encrypted communication.
     */
    void beginHandshake() throws SSLException;

    /**
     * @return true if handshake is initiated and consequent data will be encrypted.
     */
    boolean isEncrypted();
}
