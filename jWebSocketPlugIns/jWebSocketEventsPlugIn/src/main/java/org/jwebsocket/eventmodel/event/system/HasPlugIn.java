//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.event.system;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * Giving a custom plug-in identifier generate a response event indicating 
 * if it exists or not
 * 
 * @author kyberneees
 */
public class HasPlugIn extends C2SEvent {

	
	private String pluginId;

	/**
	 * @return The plug-in identifier
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @param pluginId The plug-in identifier to set
	 */
	@ImportFromToken(key = "plugin_id")
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
}
