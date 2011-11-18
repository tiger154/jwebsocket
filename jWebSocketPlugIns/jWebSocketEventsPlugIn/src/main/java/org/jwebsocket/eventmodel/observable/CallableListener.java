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
package org.jwebsocket.eventmodel.observable;

import java.util.concurrent.Callable;
import org.jwebsocket.eventmodel.api.IListener;

/**
 *
 * @author kyberneees
 */
public class CallableListener implements Callable<Object> {

	private IListener aListener;
	private Event aEvent;
	private ResponseEvent aResponseEvent;

	/**
	 *
	 * @param aListener
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public CallableListener(IListener aListener, Event aEvent, ResponseEvent aResponseEvent) {
		this.aListener = aListener;
		this.aEvent = aEvent;
		this.aResponseEvent = aResponseEvent;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public Object call() throws Exception {
		ObservableObject.callProcessEvent(aListener, aEvent, aResponseEvent);

		return null;
	}
}
