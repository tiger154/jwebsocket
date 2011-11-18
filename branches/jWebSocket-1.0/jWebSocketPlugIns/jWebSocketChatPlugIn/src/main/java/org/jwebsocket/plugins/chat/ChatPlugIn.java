//	---------------------------------------------------------------------------
//	jWebSocket - Chat Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.chat;

import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;

/**
 * This plug-in provides all the chat functionality.
 * @author aschulze
 */
public class ChatPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(ChatPlugIn.class);

	public ChatPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating chat plug-in...");
		}

		if (mLog.isInfoEnabled()) {
			mLog.info("Chat plug-in successfully loaded.");
		}
	}
}
