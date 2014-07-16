//	---------------------------------------------------------------------------
//	jWebSocket - Chain of Token Plug-Ins (Community Edition, CE)
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
package org.jwebsocket.plugins;

import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketServer;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.token.Token;

/**
 * instantiates the chain of token plug-ins.
 *
 * @author Alexander Schulze
 */
public class TokenPlugInChain extends BasePlugInChain {

	private static final Logger mLog = Logging.getLogger();

	/**
	 *
	 * @param aServer
	 */
	public TokenPlugInChain(WebSocketServer aServer) {
		super(aServer);
	}

	/**
	 *
	 * @param aConnector
	 */
	public void processLogon(WebSocketConnector aConnector) {
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			if (lPlugIn.getEnabled()) {
				try {
					TokenPlugIn lTokenPlugIn = ((TokenPlugIn) lPlugIn);
					lTokenPlugIn.processLogon(aConnector);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName() + " in plug-in '"
							+ ((TokenPlugIn) lPlugIn).getNamespace() + "': "
							+ lEx.getMessage());
				}
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 */
	public void processLogoff(WebSocketConnector aConnector) {
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			if (lPlugIn.getEnabled()) {
				try {
					TokenPlugIn lTokenPlugIn = ((TokenPlugIn) lPlugIn);
					lTokenPlugIn.processLogoff(aConnector);
				} catch (Exception lEx) {
					mLog.error(lEx.getClass().getSimpleName() + " in plug-in '"
							+ ((TokenPlugIn) lPlugIn).getNamespace() + "': "
							+ lEx.getMessage());
				}
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public PlugInResponse processToken(WebSocketConnector aConnector, Token aToken) {
		PlugInResponse lPlugInResponse = new PlugInResponse();
		String lNS = aToken.getNS();
		// tokens without namespace are not accepted anymore since jWebSocket 1.0a11
		if (lNS != null) {
			for (WebSocketPlugIn lPlugIn : getPlugIns()) {
				if (lPlugIn.getEnabled()) {
					try {
						TokenPlugIn lTokenPlugIn = ((TokenPlugIn) lPlugIn);
						if (lNS.equals(lTokenPlugIn.getNamespace())) {
							lTokenPlugIn.processToken(lPlugInResponse, aConnector, aToken);
						}
					} catch (Exception lEx) {
						mLog.error(lEx.getClass().getSimpleName()
								+ " in plug-in '"
								+ ((TokenPlugIn) lPlugIn).getNamespace() + "': "
								+ lEx.getMessage()
								+ ", source: '" + aConnector.getId() + "'"
								+ (null != aConnector.getUsername()
								? " (" + aConnector.getUsername() + ")"
								: " [not authenticated]")
								+ ", token: " + aToken.getLogString() + ", "
								+ Logging.getStackTraceAsString(lEx)
						);
					}
					if (lPlugInResponse.isChainAborted()) {
						break;
					}
				}
			}
		}
		return lPlugInResponse;
	}

	/**
	 *
	 * @param aPlugIn
	 * @param aReasonOfChange
	 * @param aVersion
	 * @param aReason
	 * @return
	 */
	public Boolean reloadPlugIn(WebSocketPlugIn aPlugIn, Token aReasonOfChange, String aVersion, String aReason) {
		List<WebSocketPlugIn> lPlugins = getPlugIns();

		for (int i = 0; i < lPlugins.size(); i++) {
			if (lPlugins.get(i).getId().equals(aPlugIn.getId())) {
				aPlugIn.setPlugInChain(this);
				lPlugins.get(i).setEnabled(false);
				((TokenPlugIn) lPlugins.get(i)).createReasonOfChange(aReasonOfChange, ChangeType.UPDATED, aVersion, aReason);
				lPlugins.set(i, aPlugIn);
				return true;
			}
		}
		return false;
	}
}
