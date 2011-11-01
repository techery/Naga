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

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Holds a delayed runnable for the event service.
 * <p>
 * This class implements the DelayedEvent interface and offers
 * offering the ability to cancel an event before it is executed.
 *
 * @author Christoffer Lerno
 */
class DelayedAction implements Comparable<DelayedAction>, DelayedEvent
{
	private final static AtomicLong s_nextId = new AtomicLong(0L);
	private volatile Runnable m_call;
	private final long m_time;
	private final long m_id;

	/**
	 * Creates a new delayed action.
	 *
	 * @param call the Runnable to call at a later point.
	 * @param time the time when the call should execute.
	 */
	public DelayedAction(Runnable call, long time)
	{
		m_call = call;
		m_time = time;
		m_id = s_nextId.getAndIncrement();
	}

	/**
	 * Cancels this delayed action.
	 */
	public void cancel()
	{
		m_call = null;
	}

	void run()
	{
		// First extract the runnable and put it in a local
		// variable since it is possible that m_call is
		// changed while this method is running.
		Runnable call = m_call;
		// If call != null i.e. the action is not cancelled,
		// execute the encapsulated runnable.
		if (call != null) call.run();
	}

	/**
	 * Compares one delayed action to another.
	 * <p>
	 * Comparison is first done by execution time, then on id (i.e. creation order).
	 *
	 * @param o the other delayed action.
	 * @return -1, 0, 1 depending on where this action should be compared to the other action.
	 */
	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
	public int compareTo(DelayedAction o)
	{
		if (m_time < o.m_time) return -1;
		if (m_time > o.m_time) return 1;
		if (m_id < o.m_id) return -1;
		return m_id > o.m_id ? 1 : 0;
	}

	public Runnable getCall()
	{
		return m_call;
	}

	public long getTime()
	{
		return m_time;
	}

	@Override
	public String toString()
	{
		return "DelayedAction @ " + new Date(m_time) + " [" + (m_call == null ? "Cancelled" : m_call) + "]";
	}
}
