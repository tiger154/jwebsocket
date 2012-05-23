//  ---------------------------------------------------------------------------
//  jWebSocket - SpringEventModelBuilder
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
 * @author kyberneees
 */
public class SpringEventModelBuilder implements IEventModelBuilder {

	private static Logger mLog = Logging.getLogger();

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
			mLog.debug("Building EventModel instance from: '" + Tools.expandEnvVars(lPath) + "'...");
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
