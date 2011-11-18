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
package org.jwebsocket.eventmodel.filter.annotation;

import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.filter.EventModelFilter;

/**
 *
 * @author kyberneees
 */
public class AnnotationFilter extends EventModelFilter {

	private static Logger mLog = Logging.getLogger(AnnotationFilter.class);

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void beforeCall(WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Getting all fields
		for (Method m : aEvent.getClass().getMethods()) {
			if (m.getName().startsWith("set")){
				processAnnotations(m, aConnector, aEvent);
			}
		}
	}

	/**
	 * Process existing annotations in the event fields
	 * 
	 * @param f The processing field
	 * @param aConnector The client WebSocketConnector
	 * @param aEvent The incoming C2SEvent from the client
	 * @throws Exception 
	 */
	void processAnnotations(Method m, WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Processing ImportFromToken annotations
		if (m.isAnnotationPresent(ImportFromToken.class)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug(">> Processing annotation '" +
						ImportFromToken.class.toString() + "' in method '"
						+ m.getName() + "'...");
			}
			//Processing the annotation...
			processImportFromToken(m, aConnector, aEvent);
		}
	}

	/**
	 * Process the ImportFromToken annotation
	 * 
	 * @param f
	 * @param aConnector
	 * @param aEvent
	 * @throws Exception
	 */
	public void processImportFromToken(Method m, WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Getting fields with the "ImportFromToken" annotation
		ImportFromToken annotation = m.getAnnotation(ImportFromToken.class);
		Object value;
		String methodName = m.getName().subSequence(3,4).toString().toLowerCase() + m.getName().substring(4);
		String key = (annotation.key().isEmpty()) ? methodName : annotation.key();

		//Importing parameter if exists
		if (aEvent.getArgs().getMap().containsKey(key)) {
			//Getting the value
			value = aEvent.getArgs().getObject(key);

			//Processing the importing strategy
			if (annotation.strategy().equals("move")) {
				aEvent.getArgs().remove(key);
			}

			//Invoking the setter method for the annotated field
			m.invoke(aEvent, value.getClass().cast(value));
		} 
	}
}
