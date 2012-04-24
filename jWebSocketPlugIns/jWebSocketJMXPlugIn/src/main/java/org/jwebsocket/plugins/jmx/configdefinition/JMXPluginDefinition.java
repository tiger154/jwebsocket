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

/**
 * Class that contains the specific elements to create the plugin object to 
 * export.
 * 
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
 */
public class JMXPluginDefinition extends JMXDefinition {

	private String mPluginId;
	private String mServerId;

	public JMXPluginDefinition() {
	}

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

	public String getPluginId() {
		if (this.mPluginId != null) {
			return mPluginId;
		} else {
			throw new IllegalArgumentException("The plugin id must not be null.");
		}
	}

	public void setPluginId(String aPluginId) {
		if (!aPluginId.equals("")) {
			this.mPluginId = aPluginId;
		} else {
			throw new IllegalArgumentException("The plugin id must not be empty.");
		}
	}

	public String getServerId() {
		if (this.mServerId != null) {
			return mServerId;
		} else {
			throw new IllegalArgumentException("The server id must not be null.");
		}
	}

	public void setServerId(String aServerId) {
		if (!aServerId.equals("")) {
			this.mServerId = aServerId;
		} else {
			throw new IllegalArgumentException("The server id must not be empty.");
		}
	}
}
