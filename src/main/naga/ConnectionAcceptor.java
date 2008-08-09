package naga;

/**
 * @author Christoffer Lerno
 * @version $Revision$ $Date$   $Author$
 */
public interface ConnectionAcceptor
{
	ConnectionAcceptor DENY = new ConnectionAcceptor()
	{
		public boolean acceptConnection(String ip)
		{
			return false;
		}
	};

	ConnectionAcceptor ALLOW = new ConnectionAcceptor()
	{
		public boolean acceptConnection(String ip)
		{
			return true;
		}
	};

	boolean acceptConnection(String ip);
}
