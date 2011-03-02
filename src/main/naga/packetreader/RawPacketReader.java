package naga.packetreader;

import naga.PacketReader;
import naga.exception.ProtocolViolationException;

import java.nio.ByteBuffer;

/**
 * This packet reader reads as many bytes as possible from the stream
 * and then bundles those bytes into a packet.
 *
 * @author Christoffer Lerno
 */
public class RawPacketReader implements PacketReader
{
	public final static int DEFAULT_BUFFER_SIZE = 256;
	private final ByteBuffer m_buffer;
    private byte[] m_packet;

	/**
	 * Create a new reader instance. With a given read buffer size (this is
	 * how many bytes the packet reader will max read in a single pass).
	 *
	 * @param bufferSize the buffer size to use.
	 */
	public RawPacketReader(int bufferSize)
	{
		m_buffer = ByteBuffer.allocate(bufferSize);
        m_packet = null;
	}

	/**
	 * Create a new reader instance with the default buffer size.
	 */
	public RawPacketReader()
	{
		this(DEFAULT_BUFFER_SIZE);
	}

    public void prepareBuffer() throws ProtocolViolationException
    {
        m_buffer.clear();
    }

    public ByteBuffer getBuffer()
	{
		return m_buffer;
	}

    public void readFinished()
    {
        if (m_buffer.position() == 0) return;
        m_buffer.flip();
        m_packet = new byte[m_buffer.remaining()];
        m_buffer.put(m_packet);
    }

    public byte[] getNextPacket() throws ProtocolViolationException
	{
        byte[] packet = m_packet;
        m_packet = null;
        return packet;
	}
}
