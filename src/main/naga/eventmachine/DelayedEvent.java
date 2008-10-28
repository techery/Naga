package naga.eventmachine;

/**
 * @author Christoffer Lerno
 */
public interface DelayedEvent
{
	/**
	 * Cancels this delayed event.
	 * <p>
	 * Note that cancelling a delayed event is *not* guaranteed to
	 * remove the event from the queue. But it is guaranteed to
	 * clear the reference to the Runnable associated with the event.
	 * The method may be called multiple times with no ill effect.
	 * <p>
	 * Cancelling an event while it is executing will not prevent it
	 * from executing.
	 * <p>
	 * <em>This metod is thread-safe.</em>
	 */
	void cancel();

	/**
	 * Returns the actual Runnable to be executed when this event runs.
	 * <p>
	 * This will value will be null if this event has been cancelled.
	 *
	 * @return the call to execute with this event runs, or null if this
	 * event is cancelled.
	 */
	Runnable getCall();


	/**
	 * Returns the time when this event will execute. See Date#getTime().
	 *
	 * @return a long representing the time when this event will occur.
	 */
	long getTime();
}
