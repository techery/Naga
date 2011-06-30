package naga;

/**
 * Implemented by observers of event exceptions.
 * <p>
 * The notifyExceptionThrown will be called synchronously
 * whn an exception is thrown.
 *
 * @author Christoffer Lerno
 */
public interface ExceptionObserver
{
	ExceptionObserver DEFAULT = new ExceptionObserver()
	{
		@SuppressWarnings({"CallToPrintStackTrace"})
		public void notifyExceptionThrown(Throwable e)
		{
			e.printStackTrace();
		}
	};

	/**
	 * Notify the observer that an exception has been thrown.
	 *
	 * @param e the exception that was thrown.
	 */
	void notifyExceptionThrown(Throwable e);
}
