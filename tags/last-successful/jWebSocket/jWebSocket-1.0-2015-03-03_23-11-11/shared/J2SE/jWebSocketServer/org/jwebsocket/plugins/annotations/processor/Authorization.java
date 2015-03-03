//	---------------------------------------------------------------------------
//	jWebSocket - Authorization (Community Edition, CE)
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
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.annotations.Authenticated;
import org.jwebsocket.plugins.annotations.Role;
import org.jwebsocket.plugins.annotations.Roles;
import org.jwebsocket.plugins.system.SecurityHelper;
import org.jwebsocket.plugins.system.SystemPlugIn;
import org.springframework.util.Assert;

/**
 * Authorization related annotations processor
 *
 * @author kyberneees
 */
public class Authorization implements IAnnotationProcessor {

	private static final List<Class> mAnnotations;

	static {
		mAnnotations = new ArrayList<Class>();
		mAnnotations.add(Roles.class);
		mAnnotations.add(Role.class);
		mAnnotations.add(Authenticated.class);
	}

	@Override
	public boolean supports(Class aAnnotationType) {
		return mAnnotations.contains(aAnnotationType);
	}

	@Override
	public void processAnnotation(Annotation aAnnotation, Object aTarget, Object[] aArgs) throws Exception {
		WebSocketConnector lConnector = (WebSocketConnector) aArgs[0];
		Class lClazz = aAnnotation.annotationType();

		if (lClazz.equals(Role.class)) {
			Assert.isTrue(SecurityHelper.userHasAuthority(lConnector, ((Role) aAnnotation).name()),
					"access denied");
		} else if (lClazz.equals(Authenticated.class)) {
			Assert.isTrue(null != lConnector.getUsername() && !SystemPlugIn.ANONYMOUS_USER.equals(lConnector.getUsername()),
					"access denied");
		} else if (lClazz.equals(Roles.class)) {
			String[] lAuthorities = ((Roles) aAnnotation).names();
			boolean lRequireAll = ((Roles) aAnnotation).requireAll();

			for (String lAuthority : lAuthorities) {
				boolean lHasAuth = SecurityHelper.userHasAuthority(lConnector, lAuthority);
				if (lRequireAll && !lHasAuth) {
					throw new Exception("access denied");
				} else if (lHasAuth) {
					return;
				}

			}

			throw new Exception("access denied");
		}
	}

}
