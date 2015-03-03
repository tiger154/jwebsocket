// ---------------------------------------------------------------------------
// jWebSocket - TestListener (Community Edition, CE)
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

import org.jwebsocket.eventmodel.api.IListener;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class TestListener implements IListener {

	public void processEvent(Event aEvent, ResponseEvent aResponseEvent) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(TestEvent aEvent, ResponseEvent aResponseEvent) {
		aResponseEvent.getArgs().setString("from", TestEvent.class.toString());
		aResponseEvent.getArgs().setString("thread", ThreadLocalContainer.getContainer().get());
		aResponseEvent.getArgs().setString("listener", TestListener.class.toString());

		aEvent.setProcessed(true);
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(TestEvent2 aEvent, ResponseEvent aResponseEvent) {
		aResponseEvent.getArgs().setString("from2", TestEvent2.class.toString());
	}
}
