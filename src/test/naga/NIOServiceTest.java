package naga;
/**
 * @author Christoffer Lerno 
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

		EasyMock.expect(acceptor.acceptConnection((InetSocketAddress) EasyMock.anyObject())).andReturn(true).once();
		EasyMock.replay(acceptor);

		final SocketObserver socketObserverClient = EasyMock.createMock(SocketObserver.class);
		final SocketObserver socketObserverServer = EasyMock.createMock(SocketObserver.class);

		ServerSocketObserver serverSocketObserver = new ServerSocketObserverAdapter()
		{
			public void newConnection(NIOSocket nioSocket)
			{
				nioSocket.listen(socketObserverServer);
			}
		};

		socketObserverServer.connectionOpened((NIOSocket) EasyMock.anyObject());
		EasyMock.expectLastCall().once();
		socketObserverClient.connectionOpened((NIOSocket) EasyMock.anyObject());
		EasyMock.expectLastCall().once();

		EasyMock.replay(socketObserverClient);
		EasyMock.replay(socketObserverServer);

		NIOServerSocket serverSocket = m_service.openServerSocket(new InetSocketAddress(3133), 0);
		NIOSocket socket = m_service.openSocket("localhost", 3133);
		socket.listen(socketObserverClient);
		serverSocket.listen(serverSocketObserver);
		serverSocket.setConnectionAcceptor(acceptor);
		while (serverSocket.getTotalConnections() == 0)
		{
			m_service.selectBlocking();
		}
		m_service.selectNonBlocking();
		assertEquals("[]", m_service.getQueue().toString());
		EasyMock.verify(socketObserverClient);
		EasyMock.verify(socketObserverServer);
		EasyMock.verify(acceptor);
		assertEquals(socket.getPort(), serverSocket.socket().getLocalPort());
		assertEquals(1, serverSocket.getTotalConnections());
		assertEquals(1, serverSocket.getTotalAcceptedConnections());
	}
	
	public void testAcceptRefused() throws Exception
	{
		ConnectionAcceptor acceptor = EasyMock.createMock(ConnectionAcceptor.class);

		EasyMock.expect(acceptor.acceptConnection((InetSocketAddress) EasyMock.anyObject())).andReturn(false).once();
		EasyMock.replay(acceptor);

		ServerSocketObserver serverSocketObserver = EasyMock.createMock(ServerSocketObserver.class);
		EasyMock.replay(serverSocketObserver);

		SocketObserver socketOwnerClientSide = EasyMock.createMock(SocketObserver.class);
		socketOwnerClientSide.connectionOpened((NIOSocket) EasyMock.anyObject());
		EasyMock.expectLastCall().once();
		socketOwnerClientSide.connectionBroken((NIOSocket) EasyMock.anyObject(), (Exception) EasyMock.anyObject());
		EasyMock.expectLastCall().once();
		EasyMock.replay(socketOwnerClientSide);

		NIOServerSocket serverSocket = m_service.openServerSocket(new InetSocketAddress(3134), 0);
		serverSocket.setConnectionAcceptor(acceptor);
		serverSocket.listen(serverSocketObserver);
		NIOSocket socket = m_service.openSocket("localhost", 3134);
		socket.listen(socketOwnerClientSide);

		while (socket.isOpen())
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