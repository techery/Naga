package naga;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@SuppressWarnings({"StaticMethodReferencedViaSubclass"})
public class SocketChannelResponderTest extends TestCase
{
	SocketChannelResponder m_socketChannelResponder;
	SelectionKey m_key;
	SocketChannel m_channel;
    NIOService m_nioService;

	protected void setUp() throws Exception
	{
		m_channel = EasyMock.createMock(SocketChannel.class);
		m_key = EasyMock.createMock(SelectionKey.class);
        m_nioService = EasyMock.createMock(NIOService.class);
	}

	public void testWriteExceedingMax()
	{
		EasyMock.expect(m_channel.isConnected()).andReturn(true).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();

		replay();
		m_socketChannelResponder = new SocketChannelResponder(m_nioService, m_channel, new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.setKey(m_key);
		m_socketChannelResponder.setMaxQueueSize(3);
		assertEquals(0, m_socketChannelResponder.getBytesWritten());
		assertEquals(0, m_socketChannelResponder.getWriteQueueSize());
		verify();
		reset();

        m_nioService.queue((Runnable)EasyMock.anyObject());
        EasyMock.expectLastCall();
		replay();

		// Add a small packet
		assertEquals(true, m_socketChannelResponder.write("F!".getBytes()));
		verify();

		// This fails because the queue would be too big.
		assertEquals(false, m_socketChannelResponder.write("OO".getBytes()));
	}

	public void testWrite() throws Exception
	{

		// Open a writer.

		PacketWriter writer = EasyMock.createMock(PacketWriter.class);
		EasyMock.expect(m_channel.isConnected()).andReturn(true).once();
        EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
        m_nioService.queue((Runnable)EasyMock.anyObject());
        EasyMock.expectLastCall();

		replay();
		m_socketChannelResponder = new SocketChannelResponder(m_nioService, m_channel, new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.setKey(m_key);
		m_socketChannelResponder.setPacketWriter(writer);
		assertEquals(0, m_socketChannelResponder.getBytesWritten());
		assertEquals(0, m_socketChannelResponder.getWriteQueueSize());

		verify();
		reset();
        EasyMock.expect(m_key.interestOps()).andReturn(4).atLeastOnce();
        EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).atLeastOnce();
        EasyMock.replay(writer);
        replay();

        m_socketChannelResponder.socketReadyForWrite();
        
        verify();
        reset();
        EasyMock.verify(writer);
        EasyMock.reset(writer);

	    m_nioService.queue((Runnable)EasyMock.anyObject());
        EasyMock.expectLastCall();
		replay();

		// Add a packet
		byte[] packet = "FOO!".getBytes();
		m_socketChannelResponder.write(packet);

		assertEquals(0, m_socketChannelResponder.getBytesWritten());
		assertEquals(4, m_socketChannelResponder.getWriteQueueSize());

		verify();
		reset();

        final ByteBuffer buffer = ByteBuffer.wrap(packet);
        ByteBuffer[] bufferArray = new ByteBuffer[] { buffer };
		// Write nothing of the packet.
		EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		EasyMock.expect(m_key.interestOps(SelectionKey.OP_WRITE)).andReturn(m_key).once();
		EasyMock.expect(m_channel.write(bufferArray)).andReturn(0L).once();
        EasyMock.expect(writer.write((ByteBuffer[])EasyMock.anyObject())).andReturn(bufferArray).once();

		replay();
		EasyMock.replay(writer);

		m_socketChannelResponder.socketReadyForWrite();

		assertEquals(0, m_socketChannelResponder.getBytesWritten());
		assertEquals(0, m_socketChannelResponder.getWriteQueueSize());

		EasyMock.verify(writer);
		EasyMock.reset(writer);
		verify();
		reset();

		// Write part of the packet.
		EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();
		EasyMock.expect(m_key.interestOps(SelectionKey.OP_WRITE)).andReturn(m_key).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		EasyMock.expect(m_channel.write(bufferArray, 0, 1)).andAnswer(new IAnswer<Long>()
        {
            public Long answer() throws Throwable
            {
                buffer.position(3);
                return 3L;
            }
        }).once();
		EasyMock.expect(m_channel.write(bufferArray, 0, 1)).andReturn(0L).once();
		replay();
		EasyMock.replay(writer);

		m_socketChannelResponder.socketReadyForWrite();

		assertEquals(3, m_socketChannelResponder.getBytesWritten());
		assertEquals(0, m_socketChannelResponder.getWriteQueueSize());
        assertEquals(1, buffer.remaining());
        
		EasyMock.verify(writer);
		EasyMock.reset(writer);
		verify();
		reset();

		// Finish writing the packet.
		EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();
		EasyMock.expect(m_channel.write(bufferArray, 0, 1)).andAnswer(new IAnswer<Long>()
        {
            public Long answer() throws Throwable
            {
                buffer.position(4);
                return 1L;
            }
        }).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		replay();
		EasyMock.replay(writer);

		m_socketChannelResponder.socketReadyForWrite();

		EasyMock.verify(writer);
		EasyMock.reset(writer);
		verify();
		reset();

        assertEquals(4, m_socketChannelResponder.getBytesWritten());
        assertEquals(0, m_socketChannelResponder.getWriteQueueSize());
        
		// Test Empty read
		EasyMock.expect(m_key.interestOps(0)).andReturn(m_key).once();
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
//		EasyMock.expect(writer.isEmpty()).andReturn(true).times(2);
		replay();
		EasyMock.replay(writer);

		m_socketChannelResponder.socketReadyForWrite();

		EasyMock.verify(writer);
		verify();

	}

	private void reset()
	{
        EasyMock.reset(m_nioService);
		EasyMock.reset(m_channel);
		EasyMock.reset(m_key);
	}

	private void verify()
	{
        EasyMock.verify(m_nioService);
		EasyMock.verify(m_channel);
		EasyMock.verify(m_key);
	}

	private void replay()
	{
        EasyMock.replay(m_nioService);
		EasyMock.replay(m_channel);
		EasyMock.replay(m_key);
	}

	public void testFinishConnectThrowsException() throws IOException
	{
		NIOService nioService = new NIOService();
		EasyMock.expect(m_key.interestOps(SelectionKey.OP_CONNECT)).andReturn(m_key).times(2);
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		m_key.cancel();
		EasyMock.expectLastCall().once();
		replay();
		m_socketChannelResponder = new SocketChannelResponder(nioService, SocketChannel.open(), new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.setKey(m_key);
		m_socketChannelResponder.socketReadyForConnect();
		assertEquals(true, m_socketChannelResponder.isOpen());
		nioService.selectNonBlocking();
		assertEquals(false, m_socketChannelResponder.isOpen());
		verify();
	}

	public void testCanReadThrowsException() throws IOException
	{
		NIOService nioService = new NIOService();
		EasyMock.expect(m_key.interestOps(SelectionKey.OP_CONNECT)).andReturn(m_key).times(2);
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		m_key.cancel();
		EasyMock.expectLastCall().once();
		replay();
		m_socketChannelResponder = new SocketChannelResponder(nioService, SocketChannel.open(), new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.setKey(m_key);
		m_socketChannelResponder.socketReadyForRead();
		assertEquals(true, m_socketChannelResponder.isOpen());
		nioService.selectNonBlocking();
		assertEquals(false, m_socketChannelResponder.isOpen());
		verify();
	}

	public void testCanWriteThrowsException() throws IOException
	{
		NIOService nioService = new NIOService();
		EasyMock.expect(m_key.interestOps(SelectionKey.OP_CONNECT)).andReturn(m_key).times(4);
		EasyMock.expect(m_key.interestOps()).andReturn(0).atLeastOnce();
		m_key.cancel();
		EasyMock.expectLastCall().once();
		replay();
		m_socketChannelResponder = new SocketChannelResponder(nioService, SocketChannel.open(), new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.setKey(m_key);
		m_socketChannelResponder.write(new byte[] { 0 });
		m_socketChannelResponder.socketReadyForWrite();
		assertEquals(true, m_socketChannelResponder.isOpen());
		nioService.selectNonBlocking();
		assertEquals(false, m_socketChannelResponder.isOpen());
		verify();
	}

	public void testSetKey() throws Exception
	{
		NIOService nioService = new NIOService();
		m_key.cancel();
		EasyMock.expectLastCall().once();
		replay();
		m_socketChannelResponder = new SocketChannelResponder(nioService, null, new InetSocketAddress("localhost", 123));
		m_socketChannelResponder.close();
		nioService.selectNonBlocking();
		m_socketChannelResponder.setKey(m_key);
		verify();
		assertEquals(false, m_socketChannelResponder.isOpen());
	}
}
