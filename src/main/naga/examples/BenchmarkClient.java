package naga.examples;

import naga.NIOService;
import naga.NIOSocket;
import naga.SocketObserver;
import naga.packetreader.RegularPacketReader;
import naga.packetwriter.RegularPacketWriter;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public class BenchmarkClient
{
	private static volatile int s_counter;
	private static volatile int s_complete;

	public static void main2(String... args)
	{
		try
		{

			String host = args[0];
			final int port = Integer.parseInt(args[1]);
			int passes = Integer.parseInt(args[2]);
			final InetAddress address = InetAddress.getByName(host);
			long startTime = System.currentTimeMillis();
			s_counter = passes;
			s_complete = 0;
			for (int i = 0; i < passes; i++)
			{
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							Socket s = new Socket(address, port);
							s.getOutputStream().write(new byte[]{0, 0, 127});
							byte[] bytes = new byte[127];
							s.getOutputStream().write(bytes);
							s.getInputStream().read(new byte[130]);
							s.close();
							s_complete++;
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						s_counter--;
					}
				}.start();
			}
			while (s_counter > 0)
			{
				Thread.sleep(25);
				System.out.println(s_counter + " " + s_complete);
			}
			System.out.println("Handling " + passes + " connections took " + (System.currentTimeMillis() - startTime)
			                   + " ms.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String... args)
	{
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		int passes = Integer.parseInt(args[2]);
		try
		{
			final NIOService service = new NIOService();
			Thread t = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						while (service.isOpen())
						{
							service.selectBlocking();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			t.start();
			s_counter = passes;
			s_complete = 0;
			InetAddress address = InetAddress.getByName(host);
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < passes; i++)
			{
				NIOSocket socket = service.openSocket(address, port);
				socket.setPacketWriter(new RegularPacketWriter(3, true));
				socket.setPacketReader(new RegularPacketReader(3, true));
				socket.listen(new SocketTester(i));
				Thread.sleep(25);
			}
			while (s_counter > 0)
			{
				Thread.sleep(100);
				System.out.println(s_counter + " " + s_complete);
			}
			System.out.println("Handling " + passes + " connections took " + (System.currentTimeMillis() - startTime)
			                   + " ms.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class SocketTester implements SocketObserver
	{
		private static Random s_random = new Random();
		private final byte[] m_packet;
		private boolean m_sendOk;
		private final int m_id;
		private boolean m_sendStart;

		public SocketTester(int id)
		{
			m_id = id;
			m_packet = new byte[1];
			s_random.nextBytes(m_packet);
			m_sendOk = false;
			m_sendStart = false;
		}

		public void connectionOpened(NIOSocket nioSocket)
		{
			m_sendStart = true;
			nioSocket.write(m_packet);
		}

		public void connectionBroken(NIOSocket nioSocket, Exception exception)
		{
			if (!m_sendOk)
			{
				System.err.println(m_id + " Disconnected before packet exchange. " + m_sendStart);
			}
			s_counter--;
		}

		public void packetReceived(NIOSocket socket, byte[] packet)
		{
			if (!Arrays.equals(packet, m_packet))
			{
				System.err.println("Packet mismatch!");
			}
			s_complete++;
			m_sendOk = true;
			socket.close();
		}
	}
}
