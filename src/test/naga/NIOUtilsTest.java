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
		                                                  true));
		assertEquals(0xFF00FE,
		             NIOUtils.getPacketSizeFromByteBuffer(ByteBuffer.wrap(new byte[]{(byte) 0xFE,
		                                                                             0x00,
		                                                                             (byte) 0xFF}),
		                                                  false));
	}

	public void testGetByteBufferFromPacketSizeTooBig()
	{
		try
		{
			NIOUtils.getByteBufferFromPacketSize(3, -1, true);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size is less than 0.", e.getMessage());
		}
		try
		{
			NIOUtils.getByteBufferFromPacketSize(3, 0xFFFFFF + 1, true);
			fail("Should throw exception");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size cannot be encoded into 3 byte(s).", e.getMessage());
		}
		try
		{
			NIOUtils.getByteBufferFromPacketSize(1, 0xFF + 1, true);
			fail("Payload size cannot be encoded into 3 byte(s).");
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Payload size cannot be encoded into 1 byte(s).", e.getMessage());
		}
	}

	public void testGetByteBufferFromPacketSize() throws Exception
	{
		getByteBufferFromPacketSizeTests(true);
		getByteBufferFromPacketSizeTests(false);
		assertEquals(0xFF,
		             NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(1, 0xFF, true), false));
		assertEquals(0x00,
		             NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(3, 0x00, false), true));
	}

	private void getByteBufferFromPacketSizeTests(boolean endian)
	{
		assertEquals(0xFFFFFF,
		             NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(3, 0xFFFFFF, endian),
		                                                  endian));
		assertEquals(0x000000,
		             NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(3, 0x000000, endian),
		                                                  endian));
		assertEquals(0xFFFFFE,
		             NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(3, 0xFFFFFE, endian),
		                                                  endian));
        assertEquals(Integer.MAX_VALUE,
                     NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(4, Integer.MAX_VALUE, endian),
                                                          endian));
        assertEquals(0x00000000,
                     NIOUtils.getPacketSizeFromByteBuffer(NIOUtils.getByteBufferFromPacketSize(4, 0x00000000, endian),
                                                          endian));
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