//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JQuery Demo Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
/**
 * 
 * @author Victor and Carlos
 */
package org.jwebsocket.plugins.jquery;

import java.util.Timer;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.plugins.TokenPlugIn;

public class JQueryPlugIn extends TokenPlugIn {
	// a timer to execute a task each some time

	private Timer t = new Timer();

	public JQueryPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		//jquery.clock.demo
		this.setNamespace(aConfiguration.getNamespace());
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		super.engineStarted(aEngine);
		t.schedule(new UpdateTimeTask(getServer(), getNamespace()), 1000);
	}
}
