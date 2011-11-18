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
 * Indicate to the server if the client cache aspect is enabled
 * <br>
 * This event must to be fired at the beginning of the connection,
 * is used to keep updated the client cache if the server cache change
 * 
 * @author kyberneees
 */
public class ClientCacheAspectStatus extends C2SEvent {

	
	private boolean enabled;

	/**
	 * @return <tt>TRUE</tt> if the client cache aspect is enabled, <tt>FALSE</tt> otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled Indicate if the client cache aspect is enabled
	 */
	@ImportFromToken(key = "enabled")
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}
