// ---------------------------------------------------------------------------
// jWebSocket - MBeanEnabledExporter (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx.mbeanspring;

import java.util.Map;
import javax.management.MBeanException;
import javax.management.modelmbean.ModelMBean;
import javolution.util.FastMap;
import org.jwebsocket.plugins.jmx.configdefinition.JMXDefinition;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Class that redefines certain features of Spring MBeanExporter taking into
 * account the characteristics of the module.
 *
 * @author Lisdey Perez Hernandez
 */
public class MBeanEnabledExporter extends MBeanExporter {

	/**
	 *
	 */
	protected Map<String, JMXDefinition> mDefinitions = new FastMap();

	/**
	 * The class default constructor.
	 */
	public MBeanEnabledExporter() {
	}

	/**
	 *
	 * @return ModelMBean
	 * @throws MBeanException
	 */
	@Override
	public ModelMBean createModelMBean() throws MBeanException {
		return new ModelMBeanExtension();
	}

	/**
	 *
	 * @param aKey
	 * @param aDefinition
	 */
	public void setDefinition(String aKey, JMXDefinition aDefinition) {
		this.mDefinitions.put(aKey, aDefinition);
	}

	/**
	 * Method for creating an ModelMBeanExtension object inserting the object to
	 * be exported and its metadata.
	 *
	 * @param managedResource
	 * @param beanKey
	 * @return
	 */
	@Override
	protected ModelMBean createAndConfigureMBean(Object managedResource,
			String beanKey) {
		try {
			JMXDefinition lDefinition = mDefinitions.get(beanKey);
			if (lDefinition != null) {
				ModelMBean mbean = createModelMBean();
				mbean.setModelMBeanInfo(lDefinition.createMBeanInfo());
				mbean.setManagedResource(managedResource, "ObjectReference");
				return mbean;
			}
		} catch (Exception ex) {
			throw new MBeanExportException("Could not create ModelMBean for "
					+ "managed resource [" + managedResource + "] with key '"
					+ beanKey + "'", ex);
		}
		return null;
	}
}
