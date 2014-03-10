// ---------------------------------------------------------------------------
// jWebSocket - ObservableObjectTest (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.observable;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastSet;
import static org.junit.Assert.*;
import org.junit.Test;
import org.jwebsocket.eventmodel.api.IListener;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class ObservableObjectTest {

	/**
	 *
	 */
	public ObservableObjectTest() {
	}

	/**
	 * Test of on method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOn_Class_IListener() throws Exception {
		System.out.println("on");
		Class<? extends Event> aEventClass = TestEvent.class;
		IListener aListener = new TestListener();

		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(aEventClass);
		instance.on(aEventClass, aListener);

		assertTrue(instance.hasListener(aEventClass, aListener));
	}

	/**
	 * Test of on method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOn_Collection_IListener() throws Exception {
		System.out.println("on");
		Collection<Class<? extends Event>> aEventClassCollection = new FastSet<Class<? extends Event>>();
		aEventClassCollection.add(TestEvent.class);
		aEventClassCollection.add(TestEvent2.class);
		IListener aListener = new TestListener();

		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(aEventClassCollection);
		instance.on(aEventClassCollection, aListener);

		assertTrue(instance.hasListener(TestEvent.class, aListener));
		assertTrue(instance.hasListener(TestEvent2.class, aListener));
	}

	/**
	 * Test of removeEvents method, of class ObservableObject.
	 */
	@Test
	public void testRemoveEvents_Class() {
		System.out.println("removeEvents");
		Collection<Class<? extends Event>> aEventClassCollection = new FastSet<Class<? extends Event>>();
		aEventClassCollection.add(TestEvent.class);
		aEventClassCollection.add(TestEvent2.class);

		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(aEventClassCollection);

		assertTrue(instance.hasEvent(TestEvent.class));
		assertTrue(instance.hasEvent(TestEvent2.class));

		instance.removeEvents(TestEvent.class);
		instance.removeEvents(TestEvent2.class);

		assertFalse(instance.hasEvent(TestEvent.class));
		assertFalse(instance.hasEvent(TestEvent2.class));
	}

	/**
	 * Test of removeEvents method, of class ObservableObject.
	 */
	@Test
	public void testRemoveEvents_Collection() {
		System.out.println("removeEvents");
		Collection<Class<? extends Event>> aEventClassCollection = new FastSet<Class<? extends Event>>();
		aEventClassCollection.add(TestEvent.class);
		aEventClassCollection.add(TestEvent2.class);

		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(aEventClassCollection);

		instance.removeEvents(aEventClassCollection);

		assertFalse(instance.hasEvent(TestEvent.class));
		assertFalse(instance.hasEvent(TestEvent2.class));
	}

	/**
	 * Test of un method, of class ObservableObject.
	 */
	@Test
	public void testUn_Class_IListener() {
		try {
			System.out.println("un");
			Class<? extends Event> aEventClass = TestEvent.class;
			IListener aListener = new TestListener();
			ObservableObject instance = new ObservableObjectImpl();
			instance.addEvents(aEventClass);
			instance.on(aEventClass, aListener);

			instance.un(aEventClass, aListener);

			assertFalse(instance.hasListener(aEventClass, aListener));

		} catch (Exception ex) {
			Logger.getLogger(ObservableObjectTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Test of un method, of class ObservableObject.
	 */
	@Test
	public void testUn_Collection_IListener() {
		try {
			System.out.println("un");
			Collection<Class<? extends Event>> aEventClassCollection = new FastSet<Class<? extends Event>>();
			aEventClassCollection.add(TestEvent.class);
			aEventClassCollection.add(TestEvent2.class);

			IListener aListener = new TestListener();
			ObservableObject instance = new ObservableObjectImpl();

			instance.addEvents(aEventClassCollection);
			instance.on(aEventClassCollection, aListener);

			instance.un(aEventClassCollection, aListener);

			assertFalse(instance.hasListeners(TestEvent.class));
			assertFalse(instance.hasListeners(TestEvent2.class));

		} catch (Exception ex) {
			Logger.getLogger(ObservableObjectTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Test of notify method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotify() throws Exception {
		System.out.println("notify");
		Event e = new TestEvent();

		ResponseEvent aResponseEvent = new ResponseEvent();
		boolean useThreads = false;
		ThreadLocalContainer.getContainer().set("main");

		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);
		TestListener listener = new TestListener();
		instance.on(TestEvent.class, listener);

		String expResult = TestEvent.class.toString();
		String expResult2 = "main";

		ResponseEvent result = instance.notify(e, aResponseEvent, useThreads);
		assertEquals(expResult, result.getArgs().getString("from"));
		assertEquals(expResult2, result.getArgs().getString("thread"));

		useThreads = true;
		result = instance.notify(e, aResponseEvent, useThreads);
		assertTrue(null == result.getArgs().getString("thread"));
	}

	/**
	 * Test of notifyUntil method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotifyUntil() throws Exception {
		System.out.println("notifyUntil");
		Event aEvent = new TestEvent();
		ResponseEvent aResponseEvent = new ResponseEvent();
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);
		instance.on(TestEvent.class, new TestListener()); //Set processed in TRUE
		instance.on(TestEvent.class, new TestListener2());

		instance.notifyUntil(aEvent, aResponseEvent);

		assertTrue(aResponseEvent.getArgs().getString("listener").equals(TestListener.class.toString()));
	}

	/**
	 * Test of hasListeners method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testHasListeners() throws Exception {
		System.out.println("hasListeners");
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);
		instance.on(TestEvent.class, new TestListener());
		instance.on(TestEvent.class, new TestListener2());

		assertTrue(instance.hasListeners(TestEvent.class));
	}

	/**
	 * Test of hasListener method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testHasListener() throws Exception {
		System.out.println("hasListener");
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);

		IListener l1 = new TestListener();
		IListener l2 = new TestListener2();

		instance.on(TestEvent.class, l1);
		instance.on(TestEvent.class, l2);

		assertTrue(instance.hasListener(TestEvent.class, l1));
		assertTrue(instance.hasListener(TestEvent.class, l2));
	}

	/**
	 * Test of purgeListeners method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testPurgeListeners() throws Exception {
		System.out.println("purgeListeners");
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);

		IListener l1 = new TestListener();
		IListener l2 = new TestListener2();

		instance.on(TestEvent.class, l1);
		instance.on(TestEvent.class, l2);

		instance.purgeListeners();

		assertFalse(instance.hasListeners(TestEvent.class));
	}

	/**
	 * Test of purgeEvents method, of class ObservableObject.
	 *
	 * @throws Exception
	 */
	@Test
	public void testPurgeEvents() throws Exception {
		System.out.println("purgeEvents");
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);

		IListener l1 = new TestListener();
		IListener l2 = new TestListener2();

		instance.on(TestEvent.class, l1);
		instance.on(TestEvent.class, l2);

		instance.purgeEvents();

		assertTrue(instance.getEvents().isEmpty());
		assertTrue(instance.getListeners().isEmpty());
	}

	/**
	 * Test of hasEvent method, of class ObservableObject.
	 */
	@Test
	public void testHasEvent() {
		System.out.println("hasEvent");
		ObservableObject instance = new ObservableObjectImpl();
		instance.addEvents(TestEvent.class);
		instance.addEvents(TestEvent2.class);

		assertTrue(instance.hasEvent(TestEvent.class));
		assertTrue(instance.hasEvent(TestEvent2.class));
	}

	/**
	 *
	 */
	public class ObservableObjectImpl extends ObservableObject {
	}
}
