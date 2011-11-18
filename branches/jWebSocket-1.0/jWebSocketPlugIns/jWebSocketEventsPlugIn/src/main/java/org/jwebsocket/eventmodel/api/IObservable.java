//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.api;

import org.jwebsocket.eventmodel.observable.*;
import java.util.Collection;
import org.jwebsocket.eventmodel.observable.Event;

/**
 * 
 * @author kyberneees
 */
public interface IObservable {

	/**
	 * Register a listener for various events, when some of these events is fired
	 * the listener is notified
	 * <p>
	 * The events most to be added as subject events first.
	 * 
	 * @param aEventClassCollection The events collection to be listen by the listener
	 * @param aListener The listener to register
	 * @throws Exception
	 */
	public void on(Collection<Class<? extends Event>> aEventClassCollection, IListener aListener) throws Exception;

	/**
	 * Register a listener for a custom event, when the event if fired the listener
	 * is notified.
	 * <p>
	 * The event most to be added as subject event first
	 *
	 * @param aEventClass The event to be listen by the listener
	 * @param aListener The listener to register
	 * @throws Exception
	 */
	public void on(Class<? extends Event> aEventClass, IListener aListener) throws Exception;

	/**
	 * Add a event as subject event. This event is supposed to be fired in the 
	 * future
	 * 
	 * @param aEventClass The event to be added
	 */
	public void addEvents(Class<? extends Event> aEventClass);

	/**
	 * Add a events collection as subject events. Events are supposed to be fired in the 
	 * future
	 * 
	 * @param aEventClassCollection The events collection to be added
	 */
	public void addEvents(Collection<Class<? extends Event>> aEventClassCollection);

	/**
	 * Remove a event from the subject events
	 * 
	 * @param aEventClass The event to be removed
	 */
	public void removeEvents(Class<? extends Event> aEventClass);

	/**
	 * Remove a events collection from the subject events
	 * 
	 * @param aEventClassCollection The events collection to be removed
	 */
	public void removeEvents(Collection<Class<? extends Event>> aEventClassCollection);

	/**
	 * Unregister a listener for a custom event
	 * 
	 * @param aEventClass The event to unregister the listener
	 * @param aListener The listener to be unregistered
	 */
	public void un(Class<? extends Event> aEventClass, IListener aListener);

	/**
	 * Unregister a listener for a collection of events
	 * 
	 * @param aEventClassCollection The events to unregister the listener
	 * @param aListener The listener to be unregistered
	 */
	public void un(Collection<Class<? extends Event>> aEventClassCollection, IListener aListener);

	/**
	 * Fire a custom event and notify the listener(s)
	 * 
	 * @param aEvent The event to be fired
	 * @param aResponseEvent The response event to be populated
	 * @param useThreads Indicate if the listeners can be executed in threads
	 * @return
	 * @throws Exception
	 */
	public ResponseEvent notify(Event aEvent, ResponseEvent aResponseEvent, boolean useThreads) throws Exception;

	/**
	 * Fire a custom event and notify the listener(s) until one of them 
	 * process the event. The listeners are executed using iteration. 
	 * <p>
	 * An event is processed when it <tt>processed</tt>
	 * attribute is set to <tt>TRUE</tt>
	 *
	 * @param aEvent The event to be fired
	 * @param aResponseEvent The response event to be populated
	 * @return
	 * @throws Exception
	 */
	public ResponseEvent notifyUntil(Event aEvent, ResponseEvent aResponseEvent) throws Exception;

	/**
	 * Indicates if a subject has listeners registered to a custom event
	 * <p>
	 * The event most to be added as subject event first
	 *
	 * @param aEventClass The target event
	 * @return <tt>TRUE</tt> if have listeners, <tt>FALSE</tt> otherwise  
	 * @throws Exception
	 */
	public boolean hasListeners(Class<? extends Event> aEventClass) throws Exception;

	/**
	 * Indicates if a listener is registered to a custom event
	 * 
	 * @param aEventClass The target event
	 * @param aListener The listener
	 * @return <tt>TRUE</tt> if the listener is registered, <tt>FALSE</tt> otherwise  
	 * @throws Exception
	 */
	public boolean hasListener(Class<? extends Event> aEventClass, IListener aListener) throws Exception;

	/**
	 * Removes all the listeners for all events
	 */
	public void purgeListeners();

	/**
	 * Removes all the events and all its listeners
	 */
	public void purgeEvents();

	/**
	 * Indicates if the subject fire a custom event
	 * 
	 * @param aEventClass The event 
	 * @return <tt>TRUE</tt> if the subject fire the event, <tt>FALSE</tt> otherwise  
	 */
	public boolean hasEvent(Class<? extends Event> aEventClass);
}
