// ---------------------------------------------------------------------------
// jWebSocket - JMXDefinitionException (Community Edition, CE)
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
package org.jwebsocket.plugins.jmx.configdefinition;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

/**
 * Class that allows to define an exception associated with a plugin or class
 * that will be exported. Thus if an exception is thrown when trying to create a
 * plugin or class will be show an object of this type.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXDefinitionException extends JMXDefinition {

	private String mExceptionMessage;

	/**
	 *
	 * @param aExceptionMessage
	 */
	public JMXDefinitionException(String aExceptionMessage) {
		this.mExceptionMessage = aExceptionMessage;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public ModelMBeanInfo createMBeanInfo() {
		return new ModelMBeanInfoSupport("JMXDefinitionException", mExceptionMessage,
				super.createMBeanAttributeInfoArray(),
				super.createMBeanConstructorInfoArray(),
				super.createMBeanOperationInfoArray(),
				super.createMBeanNotificationInfoArray());

	}
}