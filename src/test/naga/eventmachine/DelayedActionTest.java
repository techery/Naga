package naga.eventmachine;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.TestCase;

import java.util.Date;

public class DelayedActionTest extends TestCase
{
	DelayedAction m_delayedAction;


	public void testCompare()
	{
		DelayedAction action1 = new DelayedAction(null, 3);
		DelayedAction action2 = new DelayedAction(null, 5);
		DelayedAction action3 = new DelayedAction(null, 3);
		DelayedAction action4 = new DelayedAction(null, 2);
		assertEquals(0, action1.compareTo(action1));
		assertEquals(-1, action1.compareTo(action2));
		assertEquals(-1, action1.compareTo(action3));
		assertEquals(1, action1.compareTo(action4));
		assertEquals(1, action2.compareTo(action1));
		assertEquals(0, action2.compareTo(action2));
		assertEquals(1, action2.compareTo(action3));
		assertEquals(1, action2.compareTo(action4));
		assertEquals(1, action3.compareTo(action1));
		assertEquals(-1, action3.compareTo(action2));
		assertEquals(0, action3.compareTo(action3));
		assertEquals(1, action3.compareTo(action4));
		assertEquals(-1, action4.compareTo(action1));
		assertEquals(-1, action4.compareTo(action2));
		assertEquals(-1, action4.compareTo(action3));
		assertEquals(0, action4.compareTo(action4));
	}

	public void testDelayedActionToString() throws Exception
	{
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		assertEquals("DelayedAction @ " + date + " [Cancelled]", new DelayedAction(null, time).toString());
	}
}