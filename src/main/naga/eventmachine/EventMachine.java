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

import naga.ExceptionObserver;
import naga.NIOService;

import java.io.IOException;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * EventMachine is a simple event service for driving asynchronous and delayed tasks
 * together with the a Naga NIOService.
 * <p>
 * Creating and starting an event machine:
 * <pre>
 * EventMachine em = new EventMachine();
 * // Start our event machine thread:
 * em.start();
 * </pre>
 * Delayed execution:
 * <pre>
 * em.executeLater(new Runnable() {
 *   public void run()
 *   {
 *      // Code here will execute after 1 second on the nio thread.
 *   }
 * }, 1000);
 * </pre>
 * Asynchronous execution, i.e. putting a task from another thread
 * to be executed on the EventMachine thread.
 * <pre>
 * em.asyncExecute(new Runnable() {
 *   public void run()
 *   {
 *      // Code here will be executed on the nio thread.
 *   }
 * });
 * </pre>
 * It is possible to cancel scheduled tasks:
 * <pre>
 * // Schedule an event
 * DelayedEvent event = em.executeLater(new Runnable()
 *   public void run()
 *   {
 *      // Code to run in 1 minute.
 *   }
 * }, 60000);
 * // Cancel the event before it is executed.
 * event.cancel();
 * </pre>
 *
 * @author Christoffer Lerno
 */
public class EventMachine
{
	private final NIOService m_service;
	private final Queue<DelayedAction> m_queue;
	private Thread m_runThread;

	/**
	 * Creates a new EventMachine with an embedded NIOService.
	 *
	 * @throws IOException if we fail to set up the internal NIOService.
	 */
	public EventMachine() throws IOException
	{
		m_service = new NIOService();
		m_queue = new PriorityBlockingQueue<DelayedAction>();
		m_runThread = null;
	}

	/**
	 * Execute a runnable on the Event/NIO thread.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param runnable the runnable to execute on the server thread as soon as possible,
	 */
	public void asyncExecute(Runnable runnable)
	{
		executeLater(runnable, 0);
	}

	/**
	 * Execute a runnable on the Event/NIO thread after a delay.
	 * <p>
	 * This is the primary way to execute delayed events, typically time-outs and similar
	 * behaviour.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param runnable the runnable to execute after the given delay.
	 * @param msDelay the delay until executing this runnable.
	 * @return the delayed event created to execute later. This can be used
	 * to cancel the event.
	 */
	public DelayedEvent executeLater(Runnable runnable, long msDelay)
	{
		return queueAction(runnable, msDelay + System.currentTimeMillis());
	}

	/**
	 * Creates and queuest a delayed action for execution at a certain time.
	 *
	 * @param runnable the runnable to execute at the given time.
	 * @param time the time date when this runnable should execute.
	 * @return the delayed action created and queued.
	 */
	private DelayedAction queueAction(Runnable runnable, long time)
	{
		DelayedAction action = new DelayedAction(runnable, time);
		m_queue.add(action);
		m_service.wakeup();
		return action;
	}
	/**
	 * Execute a runnable on the Event/NIO thread after at a certain time.
	 * <p/>
	 * This is the primary way to execute scheduled events.
	 * <p/>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param runnable the runnable to execute at the given time.
	 * @param date the time date when this runnable should execute.
	 * @return the delayed event created to execute later. This can be used
	 * to cancel the event.
	 */
	public DelayedEvent executeAt(Runnable runnable, Date date)
	{
		return queueAction(runnable, date.getTime());
	}

	/**
	 * Sets the ExceptionObserver for this service.
	 * <p>
	 * The observer will receive all exceptions thrown by the underlying NIOService
	 * and by queued events.
	 * <p>
	 * <em>This method is thread-safe.</em>
	 *
	 * @param observer the observer to use, null will cause exceptions to log to stderr
	 */
	public void setObserver(ExceptionObserver observer)
	{
        getNIOService().setExceptionObserver(observer);
	}

	/**
	 * Returns the time when the next scheduled event will execute.
	 *
	 * @return a long representing the date of the next event, or Long.MAX_VALUE if
	 * no event is scheduled.
	 */
	public long timeOfNextEvent()
	{
		DelayedAction action = m_queue.peek();
		return action == null ? Long.MAX_VALUE : action.getTime();
	}

	/**
	 * Causes the event machine to start running on a separate thread together with the
	 * NIOService.
	 * <p/>
	 * Note that the NIOService should not be called (using {@link naga.NIOService#selectNonBlocking()} and related
	 * functions) on another thread if the EventMachine is used.
	 */
	public synchronized void start()
	{
		if (m_runThread != null) throw new IllegalStateException("Service already running.");
        if (!m_service.isOpen()) throw new IllegalStateException("Service has been shut down.");
		m_runThread = new Thread()
		{
			@Override
			public void run()
			{
				while (m_runThread == this)
				{
					try
					{
						select();
					}
					catch (Throwable e)
					{
						if (m_runThread == this) getNIOService().notifyException(e);
					}
				}
			}
		};
		m_runThread.start();
	}

	/**
	 * Stops the event machine thread, it may be restarted using start()
	 */
	public synchronized void stop()
	{
		if (m_runThread == null) throw new IllegalStateException("Service is not running.");
		m_runThread = null;
		m_service.wakeup();
	}

    /**
     * Stops the event machine and closes the underlying NIO service, it is not possible to
     * restart the event machine after shutdown.
     */
    public synchronized void shutdown()
    {
        if (m_runThread == null) throw new IllegalStateException("The service is not running.");
        m_service.close();
        stop();
    }

	/**
	 * Run all delayed events, then run select on the NIOService.
	 *
	 * @throws Throwable if any exception is thrown while executing events or handling IO.
	 */
	private void select() throws Throwable
	{
		// Run queued actions to be called
		while (timeOfNextEvent() <= System.currentTimeMillis())
		{
            try
            {
                runNextAction();
            }
            catch (Throwable t)
            {
                getNIOService().notifyException(t);
            }
		}
		if (timeOfNextEvent() == Long.MAX_VALUE)
        {
			m_service.selectBlocking();
		}
		else
		{
			long delay = timeOfNextEvent() - System.currentTimeMillis();
			m_service.selectBlocking(Math.max(1, delay));
		}
	}

	/**
	 * Runs the next action in the queue.
	 */
	private void runNextAction()
	{
		m_queue.poll().run();
	}

	/**
	 * Returns the NIOService used by this event service.
	 *
	 * @return the NIOService that this event service uses.
	 */
	public NIOService getNIOService()
	{
		return m_service;
	}

	/**
	 * Return the current event service queue.
	 *
	 * @return a copy of the current queue.
	 */
	public Queue<DelayedEvent> getQueue()
	{
		return new PriorityQueue<DelayedEvent>(m_queue);
	}

	/**
	 * Return the current queue size.
	 *
	 * @return the number events in the event queue.
	 */
	public int getQueueSize()
	{
		return m_queue.size();
	}

}
