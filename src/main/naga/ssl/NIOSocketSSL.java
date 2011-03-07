package naga.ssl;

import naga.NIOSocket;

import javax.net.ssl.SSLEngine;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public interface NIOSocketSSL extends NIOSocket
{
    SSLEngine getSSLEngine();
}
