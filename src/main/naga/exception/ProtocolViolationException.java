package naga.exception;

import java.io.IOException;

/**
 * Throw an exception due to unexpected data when reading packets.
 *
 * @author Christoffer Lerno
 */
public class ProtocolViolationException extends IOException
{
	private static final long serialVersionUID = 6869467292395980590L;

	/**
	 * Create a new exception.
	 *
	 * @param message exception message.
	 */
	public ProtocolViolationException(String message)
	{
		super(message);
	}

}
