//	---------------------------------------------------------------------------
//	jWebSocket - AnnotationFilter (Community Edition, CE)
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
package org.jwebsocket.eventmodel.filter.annotation;

import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;
import org.jwebsocket.eventmodel.filter.EventModelFilter;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Rolando Santamaria Maso
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
			if (lMethod.getName().startsWith("set")) {
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
				mLog.debug("Processing annotation '"
						+ ImportFromToken.class.toString() + "' in method '"
						+ aMethod.getName() + "'...");
			}
			//Processing the annotation...
			processImportFromToken(aMethod, aConnector, aEvent);
		}
	}

	/**
	 * Process the ImportFromToken annotation
	 *
	 * @param aMethod
	 * @param aConnector
	 * @param aEvent
	 * @throws Exception
	 */
	public void processImportFromToken(Method aMethod, WebSocketConnector aConnector, C2SEvent aEvent) throws Exception {
		//Getting fields with the "ImportFromToken" annotation
		ImportFromToken lAnnotation = aMethod.getAnnotation(ImportFromToken.class);
		Object lValue;
		String lMethodName = aMethod.getName().subSequence(3, 4).toString().toLowerCase() + aMethod.getName().substring(4);
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
