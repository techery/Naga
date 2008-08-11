package naga;
/**
 * @author Christoffer Lerno 
 * @version $Revision$ $Date$   $Author$
 */

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import java.io.IOException;
import java.net.InetSocketAddress;

public class NIOServiceTest extends TestCase
{
	NIOService m_service;

	public void setUp() throws IOException
	{
		m_service = new NIOService();
	}

	public void testOpenServerSocket() throws Exception
	{
		ConnectionAcceptor acceptor = EasyMock.createMock(ConnectionAcceptor.class);

		EasyMock.expect(acceptor.acceptConnection((String) EasyMock.anyObject())).andReturn(true).once();
		EasyMock.replay(acceptor);

		final SocketObserver socketObserverClient = EasyMock.createMock(SocketObserver.class);
		final SocketObserver socketObserverServer = EasyMock.createMock(SocketObserver.class);

		ServerSocketObserver serverSocketObserver = new ServerSocketObserverAdapter()
		{
			public void newConnection(NIOSocket nioSocket)
			{
				nioSocket.setObserver(socketObserverServer);
			}
		};

		socketObserverClient.notifyConnect((NIOSocket) EasyMock.anyObject());
		EasyMock.expectLastCall().once();

		EasyMock.replay(socketObserverClient);
		EasyMock.replay(socketObserverServer);

		NIOServerSocket serverSocket = m_service.openServerSocket(new InetSocketAddress(3133), 0);
		NIOSocket socket = m_service.openSocket("localhost", 3133);
		serverSocket.setObserver(serverSocketObserver);
		serverSocket.setConnectionAcceptor(acceptor);
		socket.setObserver(socketObserverClient);
		while (serverSocket.getTotalConnections() == 0)
		{
			m_service.selectBlocking();
		}
		EasyMock.verify(socketObserverClient);
		EasyMock.verify(socketObserverServer);
		EasyMock.verify(acceptor);
		assertEquals(1, serverSocket.getTotalConnections());
		assertEquals(1, serverSocket.getTotalAcceptedConnections());
	}
	
	public void testAcceptRefused() throws Exception
	{
		ConnectionAcceptor acceptor = EasyMock.createMock(ConnectionAcceptor.class);

		EasyMock.expect(acceptor.acceptConnection((String) EasyMock.anyObject())).andReturn(false).once();
		EasyMock.replay(acceptor);

		ServerSocketObserver serverSocketObserver = EasyMock.createMock(ServerSocketObserver.class);
		EasyMock.replay(serverSocketObserver);

		SocketObserver socketOwnerClientSide = EasyMock.createMock(SocketObserver.class);
		socketOwnerClientSide.notifyConnect((NIOSocket) EasyMock.anyObject());
		socketOwnerClientSide.notifyDisconnect((NIOSocket) EasyMock.anyObject());
		EasyMock.expectLastCall().once();
		EasyMock.replay(socketOwnerClientSide);

		NIOServerSocket serverSocket = m_service.openServerSocket(new InetSocketAddress(3134), 0);
		serverSocket.setConnectionAcceptor(acceptor);
		serverSocket.setObserver(serverSocketObserver);
		NIOSocket socket = m_service.openSocket("localhost", 3134);
		socket.setObserver(socketOwnerClientSide);

		while (socket.isAlive())
		{
			m_service.selectBlocking();
		}
		EasyMock.verify(serverSocketObserver);
		EasyMock.verify(acceptor);
		EasyMock.verify(socketOwnerClientSide);
		assertEquals(1, serverSocket.getTotalConnections());
		assertEquals(1, serverSocket.getTotalRefusedConnections());
	}
}