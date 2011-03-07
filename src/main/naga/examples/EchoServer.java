package naga.examples;

import naga.*;

import java.io.IOException;

/**
 * Creates a very simple echo server.
 * <p>
 * Run using {@code java naga.examples.EchoServer [port]}
 *
 * @author Christoffer Lerno
 */
public class EchoServer
{
	EchoServer()
	{}

	/**
	 * Runs the echo server.
	 *
	 * @param args command line arguments, assumed to be a 1 length string containing a port.
	 */
	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		try
		{
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(port);

			socket.listen(new ServerSocketObserverAdapter()
			{
				public void newConnection(NIOSocket nioSocket)
				{
					System.out.println("Client " + nioSocket.getIp() + " connected.");
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void packetReceived(NIOSocket socket, byte[] packet)
						{
                         	socket.write(packet);
						}

						public void connectionBroken(NIOSocket nioSocket, Exception exception)
						{
							System.out.println("Client " + nioSocket.getIp() + " disconnected.");
						}
					});
				}
			});
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			while (true)
			{
				service.selectBlocking();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
