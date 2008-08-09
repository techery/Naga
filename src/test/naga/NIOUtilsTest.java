package naga;
/**
 * @author Christoffer Lerno 
 * @version $Revision$ $Date$   $Author$
 */

import junit.framework.*;
import naga.NIOUtils;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Channel;
import java.io.IOException;

import org.easymock.classextension.EasyMock;

public class NIOUtilsTest extends TestCase
{
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
			NIOUtils.getByteBufferFromPacketSize(3, 0xFFFFFF + 1, true);
			fail("Should throw exception");
		}
		catch (Exception e)
		{
		}
		try
		{
			NIOUtils.getByteBufferFromPacketSize(1, 0xFF + 1, true);
			fail("Should throw exception");
		}
		catch (Exception e)
		{
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