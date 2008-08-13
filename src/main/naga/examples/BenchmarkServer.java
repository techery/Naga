package naga.examples;

import naga.*;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;

import java.io.IOException;
import java.util.Date;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class BenchmarkServer
{
	static volatile int s_echos;
	static volatile int s_connects;
	static volatile int s_disconnects;

	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		int backlog = Integer.parseInt(args[1]);
		try
		{
			s_echos = 0;
			s_connects = 0;
			s_disconnects = 0;
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(port, backlog);
			socket.listen(new ServerSocketObserverAdapter()
			{
				@Override
				public void acceptFailed(IOException exception)
				{
					exception.printStackTrace();
				}

				public void newConnection(NIOSocket nioSocket)
				{
					s_connects++;
					// Set a string reader for incoming packets.
					nioSocket.setPacketReader(new RegularPacketReader(3, true));
					nioSocket.setPacketWriter(new RegularPacketWriter(3, true));
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void packetReceived(NIOSocket socket, byte[] packet)
						{
							socket.write(packet);
							s_echos++;
						}

						@Override
						public void connectionBroken(NIOSocket nioSocket, Exception exception)
						{
							s_disconnects++;
						}
					});
				}
			});
			System.out.println("Benchmark server started at: " + socket.getAddress());
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);
			while (true)
			{
				service.selectBlocking();
				System.out.println(new Date() + " " + s_connects + " " + s_echos + " " + s_disconnects);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
