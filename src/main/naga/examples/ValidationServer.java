/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package naga.examples;

import naga.*;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An example validation server to validate logins.
 *
 * @author Christoffer Lerno
 */
public class ValidationServer
{
	ValidationServer() {
	}

	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		// Create a map with users and passwords.
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
					System.out.println("Received connection: " + nioSocket);

					// Set a 1 byte header regular reader.
					nioSocket.setPacketReader(new RegularPacketReader(1, true));

					// Set a 1 byte header regular writer.
					nioSocket.setPacketWriter(new RegularPacketWriter(1, true));

					// Listen on the connection.
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void packetReceived(NIOSocket socket, byte[] packet)
						{
							// We received a packet. Should contain two encoded
							// UTF strings with user and password.
							System.out.println("Login attempt from " + socket);
							try
							{
								// Let us unpack the bytes by converting the bytes to a stream.
								DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet));

								// Read the two strings.
								String user = stream.readUTF();
								String password = stream.readUTF();

								// Prepare to encode the response.
								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
								DataOutputStream out = new DataOutputStream(byteArrayOutputStream);

								if (!passwords.containsKey(user))
								{
									System.out.println("Unknown user: " + user);
									out.writeUTF("NO_SUCH_USER");
								}
								else if (!passwords.get(user).equals(password))
								{
									out.writeUTF("INCORRECT_PASS");
									System.out.println("Failed login for: " + user);
								}
								else
								{
									out.writeUTF("LOGIN_OK");
									System.out.println("Successful login for: " + user);
								}

								// Create the outgoing packet.
								out.flush();
								socket.write(byteArrayOutputStream.toByteArray());

								// Close after the packet has finished writing.
								socket.closeAfterWrite();
							}
							catch (IOException e)
							{
								// No error handling to speak of.
								socket.close();
							}
						}
					});
				}
			});
			
			// Allow all logins.
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);

			// Keep reading IO forever.
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
