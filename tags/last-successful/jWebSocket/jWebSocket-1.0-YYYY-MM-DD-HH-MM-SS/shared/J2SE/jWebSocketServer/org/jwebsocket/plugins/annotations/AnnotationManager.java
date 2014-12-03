//	---------------------------------------------------------------------------
//	jWebSocket - AnnotationManager (Community Edition, CE)
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
package org.jwebsocket.plugins.annotations;

import java.lang.annotation.Annotation;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.IAnnotationProcessor;

/**
 * Wrap a collection of annotation processors to simplify the annotations
 * processing task.
 *
 * @author kyberneees
 */
public class AnnotationManager implements IAnnotationProcessor {

	private final List<IAnnotationProcessor> mProcessors = new FastList<IAnnotationProcessor>();

	public List<IAnnotationProcessor> getProcessors() {
		return mProcessors;
	}

	public void setProcessors(List<IAnnotationProcessor> aProcessors) {
		mProcessors.addAll(aProcessors);
	}

	@Override
	public boolean supports(Class aAnnotationType) {
		for (IAnnotationProcessor lAP : mProcessors) {
			if (lAP.supports(aAnnotationType)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void processAnnotation(Annotation aAnnotation, Object aTarget, Object[] aArgs) throws Exception {
		for (IAnnotationProcessor lAP : mProcessors) {
			if (lAP.supports(aAnnotation.annotationType())) {
				lAP.processAnnotation(aAnnotation, aTarget, aArgs);
			}
		}
	}

}
