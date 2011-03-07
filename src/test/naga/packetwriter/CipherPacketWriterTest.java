package naga.packetwriter;
/**
 * Undocumented Class
 *
 * @author Christoffer Lerno
 */

import junit.framework.TestCase;
import naga.NIOUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.Key;
import java.util.Arrays;

public class CipherPacketWriterTest extends TestCase
{
    CipherPacketWriter m_cipherPacketWriter;

    byte[] merge(ByteBuffer[] buffers)
    {
        byte[] total = new byte[(int)NIOUtils.remaining(buffers)];
        int pos = 0;
        for (ByteBuffer buffer : buffers)
        {
            int len = buffer.remaining();
            buffer.get(total, pos, len);
            pos += len;
        }
        return total;
    }

    public void testWriteBlock() throws Exception
    {
        Key key = new SecretKeySpec("1234567890ABCDEF".getBytes(), "AES");
        Cipher encrypt = Cipher.getInstance("AES");
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        Cipher decrypt = Cipher.getInstance("AES");
        decrypt.init(Cipher.DECRYPT_MODE, key);
        m_cipherPacketWriter = new CipherPacketWriter(encrypt, new RegularPacketWriter(1, true));
        ByteBuffer[] result = m_cipherPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("ABC".getBytes()) });
        assertEquals("[3, 65, 66, 67]", Arrays.toString(decrypt.doFinal(merge(result))));
        result = m_cipherPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("C".getBytes()), ByteBuffer.wrap("D".getBytes()) });
        assertEquals("[2, 67, 68]", Arrays.toString(decrypt.doFinal(merge(result))));
    }

    public void testWriteStream() throws Exception
    {
        Key key = new SecretKeySpec("1234567890ABCDEF".getBytes(), "RC4");
        Cipher encrypt = Cipher.getInstance("RC4");
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        Cipher decrypt = Cipher.getInstance("RC4");
        decrypt.init(Cipher.DECRYPT_MODE, key);
        m_cipherPacketWriter = new CipherPacketWriter(encrypt, new RegularPacketWriter(1, true));
        ByteBuffer[] result = m_cipherPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("ABC".getBytes()) });
        assertEquals("[3, 65, 66, 67]", Arrays.toString(decrypt.doFinal(merge(result))));
        result = m_cipherPacketWriter.write(new ByteBuffer[] { ByteBuffer.wrap("C".getBytes()), ByteBuffer.wrap("D".getBytes()) });
        assertEquals("[2, 67, 68]", Arrays.toString(decrypt.doFinal(merge(result))));
    }

}