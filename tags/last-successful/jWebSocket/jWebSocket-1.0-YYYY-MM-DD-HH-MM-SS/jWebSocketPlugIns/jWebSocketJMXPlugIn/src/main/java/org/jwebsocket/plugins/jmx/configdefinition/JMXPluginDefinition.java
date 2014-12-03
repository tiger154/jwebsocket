// ---------------------------------------------------------------------------
// jWebSocket - JMXPluginDefinition (Community Edition, CE)
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

/**
 * Class that contains the specific elements to create the plugin object to
 * export.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXPluginDefinition extends JMXDefinition {

	private String mPluginId;
	private String mServerId;

	/**
	 *
	 */
	public JMXPluginDefinition() {
	}

	/**
	 *
	 * @param aPluginId
	 * @param aServerId
	 * @param aClassName
	 * @param aJarName
	 * @param aAttributes
	 * @param aOperations
	 * @param aConstructors
	 * @param aNotifications
	 */
	public JMXPluginDefinition(String aPluginId, String aServerId,
			String aClassName, String aJarName, AttributeDefinition[] aAttributes,
			OperationDefinition[] aOperations, ConstructorDefinition[] aConstructors,
			NotificationDefinition[] aNotifications) {
		super(aClassName, aJarName, aAttributes, aOperations, aConstructors,
				aNotifications);

		if (!aPluginId.equals("")) {
			this.mPluginId = aPluginId;
		} else {
			throw new IllegalArgumentException("The plugin id must not be empty");
		}

		if (!aServerId.equals("")) {
			this.mServerId = aServerId;
		} else {
			throw new IllegalArgumentException("The server id must not be empty.");
		}
	}

	/**
	 *
	 * @return
	 */
	public String getPluginId() {
		if (this.mPluginId != null) {
			return mPluginId;
		} else {
			throw new IllegalArgumentException("The plugin id must not be null.");
		}
	}

	/**
	 *
	 * @param aPluginId
	 */
	public void setPluginId(String aPluginId) {
		if (!aPluginId.equals("")) {
			this.mPluginId = aPluginId;
		} else {
			throw new IllegalArgumentException("The plugin id must not be empty.");
		}
	}

	/**
	 *
	 * @return
	 */
	public String getServerId() {
		if (this.mServerId != null) {
			return mServerId;
		} else {
			throw new IllegalArgumentException("The server id must not be null.");
		}
	}

	/**
	 *
	 * @param aServerId
	 */
	public void setServerId(String aServerId) {
		if (!aServerId.equals("")) {
			this.mServerId = aServerId;
		} else {
			throw new IllegalArgumentException("The server id must not be empty.");
		}
	}
}
