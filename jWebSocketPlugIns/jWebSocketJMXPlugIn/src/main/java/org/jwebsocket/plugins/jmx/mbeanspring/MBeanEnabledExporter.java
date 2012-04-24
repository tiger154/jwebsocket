// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
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
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class MBeanEnabledExporter extends MBeanExporter {

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
