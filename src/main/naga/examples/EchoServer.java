package naga.examples;

import naga.*;
import naga.packetreader.AsciiLinePacketReader;

import java.io.IOException;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class EchoServer
{
	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		try
		{
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(port);
			socket.setObserver(new ServerSocketObserverAdapter()
			{

				public void newConnection(NIOSocket nioSocket)
				{
					// Set a string reader for incoming packets.
					nioSocket.setPacketReader(new AsciiLinePacketReader());
					nioSocket.setObserver(new SocketObserverAdapter()
					{
						public void notifyReadPacket(NIOSocket socket, byte[] packet)
						{
							socket.write(packet);
							socket.write("\n".getBytes());
						}

						public void notifyDisconnect(NIOSocket nioSocket)
						{
							System.out.println("Bye");
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
		}
	}

}
