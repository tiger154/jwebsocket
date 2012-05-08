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

import org.jwebsocket.eventmodel.api.IListener;

/**
 *
 * @author kyberneees
 */
public class TestListener implements IListener {

	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void processEvent(TestEvent aEvent, ResponseEvent aResponseEvent) {
		aResponseEvent.getArgs().setString("from", TestEvent.class.toString());
		aResponseEvent.getArgs().setString("thread", ThreadLocalContainer.getContainer().get());
		aResponseEvent.getArgs().setString("listener", TestListener.class.toString());

		aEvent.setProcessed(true);
	}

	public void processEvent(TestEvent2 aEvent, ResponseEvent aResponseEvent) {
		aResponseEvent.getArgs().setString("from2", TestEvent2.class.toString());
	}
}
