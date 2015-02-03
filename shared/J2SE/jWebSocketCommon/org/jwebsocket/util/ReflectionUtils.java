//	---------------------------------------------------------------------------
//	jWebSocket ReflectionUtils (Community Edition, CE)
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
package org.jwebsocket.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Java reflection utility library.
 *
 * @author Rolando Santamaria Maso
 */
public class ReflectionUtils {

	public static class MethodParameter {

		private final Class mType;
		private final Annotation[] mAnnotations;

		public Annotation[] getAnnotations() {
			return mAnnotations;
		}

		/**
		 * Get parameter class type.
		 *
		 * @return
		 */
		public Class getType() {
			return mType;
		}

		public MethodParameter(Class aType, Annotation[] aAnnotations) {
			mType = aType;
			mAnnotations = aAnnotations;
		}

		/**
		 * Get annotation given custom annotation type.
		 *
		 * @param aAnnotationType
		 * @return
		 */
		public Annotation getAnnotation(Class aAnnotationType) {
			for (Annotation lA : mAnnotations) {
				if (lA.annotationType().equals(aAnnotationType)) {
					return lA;
				}
			}

			return null;
		}
	}

	/**
	 * Get method parameters.
	 *
	 * @param aMethod
	 * @return
	 */
	public static List<MethodParameter> getMethodParameters(Method aMethod) {
		Class<?>[] lParameterTypes = aMethod.getParameterTypes();
		Annotation[][] lAnnotations = aMethod.getParameterAnnotations();

		List<MethodParameter> lParameters = new ArrayList<ReflectionUtils.MethodParameter>();
		for (int lIndex = 0; lIndex < lParameterTypes.length; lIndex++) {
			lParameters.add(new MethodParameter(lParameterTypes[lIndex], lAnnotations[lIndex]));
		}

		return lParameters;
	}
}
