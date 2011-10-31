package naga.examples;

import naga.NIOService;
import naga.NIOSocket;
import naga.NIOSocketSSL;
import naga.SocketObserver;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */
public class SSLTester
{
    public static void main(String... args)
    {
        try
        {
            NIOService service = new NIOService();
            SSLEngine engine = SSLContext.getDefault().createSSLEngine();
            NIOSocket socket = service.openSSLSocket(engine, "www.sslshopper.com", 443);
            socket.listen(new SocketObserver() {
                public void packetSent(NIOSocket socket, Object tag)
                {
                    System.out.println("Packet sent");
                }

                public void connectionOpened(NIOSocket nioSocket)
                {
                    try
                    {
                        ((NIOSocketSSL)nioSocket).beginHandshake();
                    }
                    catch (SSLException e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println("*Connection opened");
                    nioSocket.write("GET /ssl-converter.html HTTP/1.0\r\n\r\n".getBytes());
                }

                public void connectionBroken(NIOSocket nioSocket, Exception exception)
                {
                    System.out.println("*Connection broken");
                    if (exception != null) exception.printStackTrace();
                    System.exit(9);
                }

                public void packetReceived(NIOSocket socket, byte[] packet)
                {
                    System.out.println("*Unencrypted Packet received " + packet.length);
                    System.out.println(new String(packet));
                }
            });
           //         https://www.sslshopper.com/ssl-converter.html
            while (true)
            {
                service.selectBlocking();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
