package naga.examples;

import naga.*;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class ValidationServer
{
	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		final Map<String, String> passwords = new HashMap<String, String>();
		passwords.put("Admin", "password");
		passwords.put("Aaron", "AAAAAAAA");
		passwords.put("Bob", "QWERTY");
		passwords.put("Lisa", "secret");
		try
		{
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(port);
			socket.listen(new ServerSocketObserverAdapter()
			{
				public void newConnection(NIOSocket nioSocket)
				{
					// Set a string reader for incoming packets.
					nioSocket.setPacketReader(new RegularPacketReader(1, true));
					nioSocket.setPacketWriter(new RegularPacketWriter(1, true));
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void packetReceived(NIOSocket socket, byte[] packet)
						{
							try
							{
								DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet));
								String user = stream.readUTF();
								String password = stream.readUTF();
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
								if (!passwords.containsKey(user))
								{
									out.writeUTF("NO_SUCH_USER");
									return;
								}
								if (!passwords.get(user).equals(password))
								{
									out.writeUTF("INCORRECT_PASS");
									System.out.println("Failed login for " + user);
								}
								out.writeUTF("WELCOME");
								System.out.println("Successful login for " + user);
								out.flush();
								socket.write(byteArrayOutputStream.toByteArray());
								socket.closeAfterWrite();
							}
							catch (IOException e)
							{
								//e.printStackTrace();
								//System.exit(0);
								socket.close();
							}
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
