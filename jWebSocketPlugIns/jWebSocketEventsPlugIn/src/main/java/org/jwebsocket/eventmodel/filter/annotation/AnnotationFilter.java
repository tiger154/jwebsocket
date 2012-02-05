//  ---------------------------------------------------------------------------
//  jWebSocket - AnnotationFilter
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
		for (Method lMethod : aEvent.getClass().getMethods()) {
			if (lMethod.getName().startsWith("set")){
				processAnnotations(lMethod, aConnector, aEvent);
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
	void processAnnotations(Method aMethod, WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Processing ImportFromToken annotations
		if (aMethod.isAnnotationPresent(ImportFromToken.class)) {
			if (mLog.isDebugEnabled()) {
				mLog.debug("Processing annotation '" +
						ImportFromToken.class.toString() + "' in method '"
						+ aMethod.getName() + "'...");
			}
			//Processing the annotation...
			processImportFromToken(aMethod, aConnector, aEvent);
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
	public void processImportFromToken(Method aMethod, WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Getting fields with the "ImportFromToken" annotation
		ImportFromToken lAnnotation = aMethod.getAnnotation(ImportFromToken.class);
		Object lValue;
		String lMethodName = aMethod.getName().subSequence(3,4).toString().toLowerCase() + aMethod.getName().substring(4);
		String lKey = (lAnnotation.key().isEmpty()) ? lMethodName : lAnnotation.key();

		//Importing parameter if exists
		if (aEvent.getArgs().getMap().containsKey(lKey)) {
			//Getting the value
			lValue = aEvent.getArgs().getObject(lKey);

			//Processing the importing strategy
			if (lAnnotation.strategy().equals("move")) {
				aEvent.getArgs().remove(lKey);
			}

			//Invoking the setter method for the annotated field
			aMethod.invoke(aEvent, lValue.getClass().cast(lValue));
		} 
	}
}
