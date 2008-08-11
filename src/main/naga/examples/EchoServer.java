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

			socket.listen(new ServerSocketObserverAdapter()
			{
				public void newConnection(NIOSocket nioSocket)
				{
					System.out.println("Client " + nioSocket.getIp() + " connected.");
					nioSocket.write("Welcome to the Echo Server\n".getBytes());
					// Set a string reader for incoming packets.
					nioSocket.setPacketReader(new AsciiLinePacketReader());
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void notifyReadPacket(NIOSocket socket, byte[] packet)
						{
							String string = new String(packet).trim();
							System.out.println("Echo: '" + string + "' to " + socket.getIp());
							if (string.equals("~BYE"))
							{
								socket.write(("Goodbye " + socket.getIp() + "\n").getBytes());
								socket.closeAfterWrite();
								return;
							}
							socket.write(string.getBytes());
							socket.write("\n".getBytes());
						}

						public void notifyDisconnect(NIOSocket nioSocket)
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
