//	---------------------------------------------------------------------------
//	jWebSocket - Dependency (Community Edition, CE)
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
package org.jwebsocket.plugins.annotations.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.jwebsocket.api.IAnnotationProcessor;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.plugins.annotations.RequireConnection;
import org.jwebsocket.plugins.annotations.RequirePlugIn;
import org.jwebsocket.plugins.annotations.RequirePlugIns;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.ConnectionManager;
import org.springframework.util.Assert;

/**
 * Dependency related annotations processor
 *
 * @author kyberneees
 */
public class Dependency implements IAnnotationProcessor {

	private static final List<Class> mAnnotations;

	static {
		mAnnotations = new ArrayList<Class>();
		mAnnotations.add(RequireConnection.class);
		mAnnotations.add(RequirePlugIn.class);
		mAnnotations.add(RequirePlugIns.class);
	}

	@Override
	public boolean supports(Class aAnnotationType) {
		return mAnnotations.contains(aAnnotationType);
	}

	@Override
	public void processAnnotation(Annotation aAnnotation, Object aTarget, Object[] aArgs) throws Exception {
		Class lClazz = aAnnotation.annotationType();

		if (lClazz.equals(RequireConnection.class)) {
			String lConnectionName = ((RequireConnection) aAnnotation).name();
			ConnectionManager lConnManager = (ConnectionManager) JWebSocketBeanFactory.getInstance()
					.getBean(JWebSocketServerConstants.CONNECTION_MANAGER_BEAN_ID);
			Assert.isTrue(lConnManager.isValid(lConnectionName), "Required '" + lConnectionName
					+ "' connection is not valid!");
		} else if (lClazz.equals(RequirePlugIn.class)) {
			String lPlugInId = ((RequirePlugIn) aAnnotation).id();
			Assert.notNull(JWebSocketFactory.getTokenServer().getPlugInById(lPlugInId),
					"Required '" + lPlugInId + "' plug-in not found!");
		} else if (lClazz.equals(RequirePlugIns.class)) {
			String[] lPlugInsId = ((RequirePlugIns) aAnnotation).ids();
			for (String lId : lPlugInsId) {
				Assert.notNull(JWebSocketFactory.getTokenServer().getPlugInById(lId),
						"Required '" + lId + "' plug-in not found!");
			}

		}

	}

}
