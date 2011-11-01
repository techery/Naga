/*
Copyright (c) 2008-2011 Christoffer Lernö

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package naga.eventmachine;

/**
 * A cancellable, delayed event posted to the event service.
 *
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
