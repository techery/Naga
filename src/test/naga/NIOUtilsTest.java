package naga;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

public class NIOUtilsTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testInstance()
	{
		new NIOUtils();
	}

	public void testGetPacketSizeFromByteBuffer() throws Exception
	{
		assertEquals(0xFF00FE,
		             NIOUtils.getPacketSizeFromByteBuffer(ByteBuffer.wrap(new byte[]{(byte) 0xFF,
		                                                                             0x00,
		                                                                             (byte) 0xFE}),
		                                                  3,
                                                          true));
		assertEquals(0xFF00FE,
		             NIOUtils.getPacketSizeFromByteBuffer(ByteBuffer.wrap(new byte[]{(byte) 0xFE,
		                                                                             0x00,
		                                                                             (byte) 0xFF}),
		                                                  3,
                                                          false));
	}

	public void testSetPacketSizeInByteBufferTooBig()
	{
        ByteBuffer buffer = ByteBuffer.allocate(10);
		try
		{
			NIOUtils.setPacketSizeInByteBuffer(buffer, 3, -1, true);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size is less than 0.", e.getMessage());
		}
		try
		{
			NIOUtils.setPacketSizeInByteBuffer(buffer, 3, 0xFFFFFF + 1, true);
			fail("Should throw exception");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size cannot be encoded into 3 byte(s).", e.getMessage());
		}
		try
		{
			NIOUtils.setPacketSizeInByteBuffer(buffer, 1, 0xFF + 1, true);
			fail("Payload size cannot be encoded into 3 byte(s).");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size cannot be encoded into 1 byte(s).", e.getMessage());
		}
	}

	public void testSetPacketSizeInByteBuffer() throws Exception
	{
        ByteBuffer buffer = ByteBuffer.allocate(10);
		getByteBufferFromPacketSizeTests(true);
		getByteBufferFromPacketSizeTests(false);
        NIOUtils.setPacketSizeInByteBuffer(buffer, 1, 0xFF, true);
        buffer.flip();
		assertEquals(0xFF, NIOUtils.getPacketSizeFromByteBuffer(buffer, 1, false));
        buffer.clear();
        NIOUtils.setPacketSizeInByteBuffer(buffer, 3, 0x00, false);
        buffer.flip();
		assertEquals(0x00, NIOUtils.getPacketSizeFromByteBuffer(buffer, 3, true));
	}

	private void getByteBufferFromPacketSizeTests(boolean endian)
	{
        ByteBuffer buffer = ByteBuffer.allocate(10);
        NIOUtils.setPacketSizeInByteBuffer(buffer, 3, 0xFFFFFF, endian);
        buffer.flip();
		assertEquals(0xFFFFFF, NIOUtils.getPacketSizeFromByteBuffer(buffer, 3, endian));
        buffer.clear();
        NIOUtils.setPacketSizeInByteBuffer(buffer, 3, 0x000000, endian);
        buffer.flip();
		assertEquals(0x000000, NIOUtils.getPacketSizeFromByteBuffer(buffer, 3, endian));
        buffer.clear();
        NIOUtils.setPacketSizeInByteBuffer(buffer, 3, 0xFFFFFE, endian);
        buffer.flip();
		assertEquals(0xFFFFFE, NIOUtils.getPacketSizeFromByteBuffer(buffer, 3, endian));
        buffer.clear();
        NIOUtils.setPacketSizeInByteBuffer(buffer, 4, Integer.MAX_VALUE, endian);
        buffer.flip();
        assertEquals(Integer.MAX_VALUE, NIOUtils.getPacketSizeFromByteBuffer(buffer, 4, endian));
        buffer.clear();
        NIOUtils.setPacketSizeInByteBuffer(buffer, 4, 0x00000000, endian);
        buffer.flip();
        assertEquals(0x00000000, NIOUtils.getPacketSizeFromByteBuffer(buffer, 4, endian));
	}

	public void testCancelKeySilently() throws Exception
	{
		SelectionKey key = EasyMock.createMock(SelectionKey.class);
		key.cancel();
		EasyMock.expectLastCall().andThrow(new RuntimeException());
		EasyMock.replay(key);
		NIOUtils.cancelKeySilently(key);
		EasyMock.verify(key);
	}

	public void testCloseChannelSilently() throws Exception
	{
		Channel channel = EasyMock.createMock(Channel.class);
		channel.close();
		EasyMock.expectLastCall().andThrow(new IOException());
		EasyMock.replay(channel);
		NIOUtils.closeChannelSilently(channel);
		EasyMock.verify(channel);
	}

}