//	---------------------------------------------------------------------------
//	jWebSocket - SpringEventModelBuilder (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.events;

import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.api.IEventModelBuilder;
import org.jwebsocket.eventmodel.core.EventModel;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.Tools;

/**
 * Build the EventModel instance by using the Spring IoC Container
 *
 * @author Rolando Santamaria Maso
 */
public class SpringEventModelBuilder implements IEventModelBuilder {

	private static final Logger mLog = Logging.getLogger();

	/**
	 * Uses Spring IoC Container to build the EventModel instance
	 *
	 * @param aPlugIn
	 * @return
	 * @throws Exception
	 */
	@Override
	public EventModel build(EventsPlugIn aPlugIn) throws Exception {
		ClassLoader lClassLoader = getClass().getClassLoader();
		String lPath =
				"${JWEBSOCKET_HOME}conf/EventsPlugIn/"
				+ aPlugIn.getNamespace() + "-application/bootstrap.xml";

		if (mLog.isDebugEnabled()) {
			mLog.debug("Building EventModel instance from: '" + Tools.expandEnvVarsAndProps(lPath) + "'...");
		}

		JWebSocketBeanFactory.load(aPlugIn.getNamespace(), lPath, lClassLoader);

		EventModel lEM = (EventModel) JWebSocketBeanFactory.getInstance(aPlugIn.getNamespace()).getBean("EventModel");

		// Initializing the event model  
		lEM.setParent(aPlugIn);
		lEM.initialize();

		if (mLog.isDebugEnabled()) {
			mLog.debug("EventModel instance for '" + aPlugIn.getNamespace()
					+ "' application build successful!");
		}

		return lEM;
	}
}
