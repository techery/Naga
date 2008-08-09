package naga;

import java.nio.channels.SelectionKey;
import java.nio.channels.Channel;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface ChannelResponder
{
	void notifyCanRead();

	void notifyCanWrite();

	void notifyCanAccept();

	void notifyCanConnect();

	void notifyWasCancelled();
}
