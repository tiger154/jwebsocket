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
package org.jwebsocket.plugins.remoteshell;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.IOptionName;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;

/**
 *
 * @author yasmany
 */
public class RemoteShellPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(RemoteShellPlugIn.class);
	private ConnBean mCb;
	private SSHExec mSsh = null;

	public RemoteShellPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating RemoteShellPlugin...");
		}
		this.setNamespace(aConfiguration.getNamespace());
		if (mLog.isDebugEnabled()) {
			mLog.debug("Plugin RemoteShellPlugin instatiated correctly!");
		}
	}

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		super.connectorStarted(aConnector);
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		super.connectorStopped(aConnector, aCloseReason);
		mSsh.disconnect();
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(this.getNamespace())) {
			try {
				if (aToken.getType().equals("register")) {
					SSHExec.setOption(IOptionName.INTEVAL_TIME_BETWEEN_TASKS, 0l);
					// Initialize a ConnBean object, parameter list is ip, username, password
					mCb = new ConnBean(aToken.getString("host"), aToken.getString("user"), aToken.getString("password"));
					mSsh = SSHExec.getInstance(mCb);
					mSsh.connect();
				}
				if (aToken.getType().equals("disconnect")) {
					mSsh.disconnect();
				}
				if (aToken.getType().equals("exec_command")) {
					CustomTask lCommand = new ExecCommand(aToken.getString("command"));
					Result lResult = mSsh.exec(lCommand);
					if (lResult.isSuccess) {
						aToken.setString("result", lResult.sysout);
					} else {
						aToken.setString("result", lResult.error_msg + "\n");
					}
					getServer().sendToken(aConnector, aToken);
				}
			} catch (Exception ex) {
				mLog.error(ex.getMessage(), ex);
			}
		}
	}
}
