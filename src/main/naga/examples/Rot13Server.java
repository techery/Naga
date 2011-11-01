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
import naga.packetreader.AsciiLinePacketReader;
import naga.packetwriter.AsciiLinePacketWriter;

import java.io.IOException;

/**
 * Creates a Rot13Server that takes a line of text and returns the Rot13 version of the
 * text.
 * <p/>
 * Run using {@code java naga.examples.Rot13Server [port]}.
 *
 * @author Christoffer Lerno
 */
public class Rot13Server
{
	Rot13Server()
	{}
	
	/**
	 * Runs the rot13 server.
	 *
	 * @param args command line arguments, assumed to be a 1 length string containing a port.
	 */
	public static void main(String... args)
	{
		int port = Integer.parseInt(args[0]);
		try
		{
			// Open the service.
			NIOService service = new NIOService();
			NIOServerSocket socket = service.openServerSocket(port);
			final byte[] welcomeMessage = ("Welcome to the ROT13 server at " + socket.toString() + "!").getBytes();

			// Start listening to the server socket.
			socket.listen(new ServerSocketObserverAdapter()
			{
				public void newConnection(NIOSocket nioSocket)
				{
					System.out.println("Client " + nioSocket.getIp() + " connected.");
					nioSocket.setPacketReader(new AsciiLinePacketReader());
                    nioSocket.setPacketWriter(new AsciiLinePacketWriter());
                    nioSocket.write(welcomeMessage);
					nioSocket.listen(new SocketObserverAdapter()
					{
						public void packetReceived(NIOSocket socket, byte[] packet)
						{
							// Convert the packet to a string and trim non-printables.
							String line = new String(packet).trim();

							// Disconnect on "+++"
							if (line.equals("+++"))
							{
								socket.write("Thank you and good bye.".getBytes());
								socket.closeAfterWrite();
								return;
							}

							// Build our ROT13 version of the incoming string.
							StringBuilder builder = new StringBuilder(line);
							for (int i = 0; i < builder.length(); i++)
							{
								char c = builder.charAt(i);
								if (c >= 'a' && c <= 'z')
								{
									builder.setCharAt(i, (char) (((c - 'a') + 13) % 26 + 'a'));
								}
								if (c >= 'A' && c <= 'Z')
								{
									builder.setCharAt(i, (char) (((c - 'A') + 13) % 26 + 'A'));
								}
							}

							// Write the result and append a new line.
							socket.write(builder.toString().getBytes());
						}

						public void connectionBroken(NIOSocket nioSocket, Exception exception)
						{
							System.out.println("Client " + nioSocket.getIp() + " disconnected.");
							if (exception != null)
							{
								exception.printStackTrace();
							}
						}
					});
				}
			});

			// Allow all connections.
			socket.setConnectionAcceptor(ConnectionAcceptor.ALLOW);

			// Read IO until process exits.
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
