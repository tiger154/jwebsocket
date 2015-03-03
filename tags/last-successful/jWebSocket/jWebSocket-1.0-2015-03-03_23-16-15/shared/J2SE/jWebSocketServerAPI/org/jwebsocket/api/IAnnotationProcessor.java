//	---------------------------------------------------------------------------
//	jWebSocket - IAnnotationProcessor (Community Edition, CE)
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
package org.jwebsocket.api;

import java.lang.annotation.Annotation;

/**
 * Annotation processors are responsible for annotations processing on target
 * objects.
 *
 * @author Rolando Santamaria Maso
 */
public interface IAnnotationProcessor {

	/**
	 * Return TRUE if the given annotation type is supported by the processor,
	 * FALSE otherwise
	 *
	 * @param aAnnotationType
	 * @return
	 */
	boolean supports(Class aAnnotationType);

	/**
	 * Process annotation
	 * 
	 * @param aAnnotation The annotation type to be processed
	 * @param aTarget The annotated object
	 * @param aArgs The calling arguments in case of target be a method
	 * @throws Exception 
	 */
	void processAnnotation(Annotation aAnnotation, Object aTarget, Object[] aArgs) throws Exception;
}
