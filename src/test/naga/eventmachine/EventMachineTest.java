package naga.eventmachine;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;
import naga.ExceptionObserver;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EventMachineTest extends TestCase
{
	EventMachine m_eventMachine;

	@Override
	protected void setUp() throws Exception
	{
		m_eventMachine = new EventMachine();
	}

	public void testExceptionHandling() throws Exception
	{
		final StringBuffer exceptions = new StringBuffer();
		try
		{
			m_eventMachine.setObserver(null);
		}
		catch (NullPointerException e)
		{
		}
		m_eventMachine.start();
		m_eventMachine.asyncExecute(new Runnable()
		{
			public void run()
			{
				throw new RuntimeException("Test1");
			}
		});
		Thread.sleep(10);
		m_eventMachine.setObserver(new ExceptionObserver()
		{
			public void notifyExceptionThrown(Throwable e)
			{
				exceptions.append(e.getMessage());
			}
		});
		m_eventMachine.asyncExecute(new Runnable()
		{
			public void run()
			{
				throw new RuntimeException("Test2");
			}
		});
		Thread.sleep(50);
		assertEquals("Test2", exceptions.toString());
	}

	public void testExecuteLaterOrdering() throws Exception
	{
		Runnable a = new Runnable()
		{
			public void run()
			{

			}
		};
		Runnable b = new Runnable()
		{
			public void run()
			{
			}
		};
		Runnable c = new Runnable()
		{
			public void run()
			{
			}
		};
		m_eventMachine.executeLater(a, 20000);
		m_eventMachine.executeLater(b, 30000);
		m_eventMachine.executeLater(c, 10000);
		Queue<DelayedEvent> actions = m_eventMachine.getQueue();
		assertEquals(actions.poll().getCall(), c);
		assertEquals(actions.poll().getCall(), a);
		assertEquals(actions.poll().getCall(), b);
	}


	public void testExecuteAtOrdering() throws Exception
	{
		Runnable a = new Runnable()
		{
			public void run()
			{

			}
		};
		Runnable b = new Runnable()
		{
			public void run()
			{
			}
		};
		Runnable c = new Runnable()
		{
			public void run()
			{
			}
		};
		Date d = new Date();
		m_eventMachine.executeAt(a, d);
		m_eventMachine.executeAt(b, d);
		m_eventMachine.executeAt(c, d);
		Queue<DelayedEvent> actions = m_eventMachine.getQueue();
		assertEquals(actions.poll().getCall(), a);
		assertEquals(actions.poll().getCall(), b);
		assertEquals(actions.poll().getCall(), c);
	}

	public void testExecute() throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		m_eventMachine.asyncExecute(new Runnable()
		{
			public void run()
			{
				latch.countDown();
			}
		});
		assertEquals(false, latch.await(50, TimeUnit.MILLISECONDS));
		m_eventMachine.start();
		assertEquals(true, latch.await(10, TimeUnit.SECONDS));
		m_eventMachine.stop();
	}

	public void testExecuteLater() throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		m_eventMachine.executeLater(new Runnable()
		{
			public void run()
			{
				latch.countDown();
			}
		}, 5);
		assertEquals(false, latch.await(50, TimeUnit.MILLISECONDS));
		m_eventMachine.start();
		assertEquals(true, latch.await(10, TimeUnit.SECONDS));
		m_eventMachine.stop();
	}

	public void testExecuteLater2() throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		m_eventMachine.executeLater(new Runnable()
		{
			public void run()
			{
				latch.countDown();
			}
		}, 50);
		m_eventMachine.start();
		assertEquals(false, latch.await(20, TimeUnit.MILLISECONDS));
		assertEquals(true, latch.await(50, TimeUnit.SECONDS));
		m_eventMachine.stop();
	}

	public void testExecuteLaterWith() throws Exception
	{
		final CountDownLatch latch = new CountDownLatch(1);
		m_eventMachine.executeLater(new Runnable()
		{
			public void run()
			{
				latch.countDown();
			}
		}, 5);
		assertEquals(false, latch.await(50, TimeUnit.MILLISECONDS));
		m_eventMachine.start();
		assertEquals(true, latch.await(10, TimeUnit.SECONDS));
		m_eventMachine.stop();
	}

	public void testNIO()
	{
		assertNotNull(m_eventMachine.getNIOService());
	}

	public void testStartStop() throws Exception
	{
		m_eventMachine.start();
		try
		{
			m_eventMachine.start();
			fail();
		}
		catch (IllegalStateException e)
		{
		}
		m_eventMachine.stop();
		try
		{
			m_eventMachine.stop();
			fail();
		}
		catch (IllegalStateException e)
		{
		}
		final CountDownLatch latch = new CountDownLatch(1);
		m_eventMachine.start();
		m_eventMachine.asyncExecute(new Runnable()
		{
			public void run()
			{
				latch.countDown();
			}
		});
		assertEquals(true, latch.await(10, TimeUnit.MILLISECONDS));
	}

	public void testCancelEvent() throws Exception
	{
		final AtomicInteger integer = new AtomicInteger();
		m_eventMachine.executeLater(new Runnable()
		{
			public void run()
			{
				integer.incrementAndGet();
			}
		}, 5);
		DelayedEvent event = m_eventMachine.executeLater(new Runnable()
		{
			public void run()
			{
				integer.addAndGet(2);
			}
		}, 5);
		event.cancel();
		assertEquals(2, m_eventMachine.getQueueSize());
		m_eventMachine.start();
		Thread.sleep(20);
		assertEquals(1, integer.intValue());
		m_eventMachine.stop();
		assertEquals(0, m_eventMachine.getQueueSize());
	}
}