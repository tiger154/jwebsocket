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
package org.jwebsocket.plugins.jmx.configdefinition;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

/**
 * Class that allows to define an exception associated with a plugin or class 
 * that will be exported. Thus if an exception is thrown when trying to create a
 * plugin or class will be show an object of this type.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXDefinitionException extends JMXDefinition {

	private String mExceptionMessage;

	public JMXDefinitionException(String aExceptionMessage) {
		this.mExceptionMessage = aExceptionMessage;
	}

	@Override
	public ModelMBeanInfo createMBeanInfo() {
		return new ModelMBeanInfoSupport("JMXDefinitionException", mExceptionMessage,
				super.createMBeanAttributeInfoArray(),
				super.createMBeanConstructorInfoArray(),
				super.createMBeanOperationInfoArray(),
				super.createMBeanNotificationInfoArray());

	}
}