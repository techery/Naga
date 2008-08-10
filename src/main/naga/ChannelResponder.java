package naga;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

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

	SelectableChannel getChannel();

	void setKey(SelectionKey key);
}
