//	---------------------------------------------------------------------------
//	jWebSocket - Parameters (Community Edition, CE)
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
import org.jwebsocket.plugins.annotations.Param;
import org.jwebsocket.plugins.annotations.Params;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.ReflectionUtils;
import org.springframework.util.Assert;

/**
 * Method parameters annotations processor
 *
 * @author Rolando Santamaria Maso
 */
public class Parameters implements IAnnotationProcessor {

	private static final List<Class> mAnnotations;

	static {
		mAnnotations = new ArrayList<Class>();
		mAnnotations.add(Param.class);
		mAnnotations.add(Parameters.class);
	}

	@Override
	public boolean supports(Class aAnnotationType) {
		return mAnnotations.contains(aAnnotationType);
	}

	@Override
	public void processAnnotation(Annotation aAnnotation, Object aTarget, Object[] aArgs) throws Exception {
		Token lToken = (Token) aArgs[1];
		Class lClazz = aAnnotation.annotationType();
		if (lClazz.equals(Param.class)) {
			Param lAnnotation = (Param) aAnnotation;
			Object lParameterValue = lToken.getObject(lAnnotation.id());
			if (lAnnotation.required()) {
				Assert.isTrue(null != lParameterValue,
						"The parameter '" + lAnnotation.id() + "' value cannot be NULL!");

			}

			// getting the expected parameter class
			Class lType = (null != aTarget && aTarget instanceof ReflectionUtils.MethodParameter)
					? ((ReflectionUtils.MethodParameter) aTarget).getType()
					: lAnnotation.type();
			try {
				// check class type
				if (null != lParameterValue) {
					lType.cast(lParameterValue);
				}
			} catch (Exception lEx) {
				throw new Exception("The given '" + lAnnotation.id() + "' parameter value,"
						+ " cannot be casted to '" + lType.getName() + "' class!");
			}
		} else if (lClazz.equals(Params.class)) {
			Params lAnnotation = (Params) aAnnotation;
			for (Param lParam : lAnnotation.params()) {
				processAnnotation(lParam, null, aArgs);
			}
		}
	}
}
