//	---------------------------------------------------------------------------
//	jWebSocket - Plug-in chain (Community Edition, CE)
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

import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.*;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketSession;
import org.jwebsocket.logging.Logging;

/**
 * Implements the basic chain of plug-ins which is triggered by a server when
 * data packets are received. Each data packet is pushed through the chain and
 * can be processed by the plug-ins.
 *
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 * @author Rolando Santamaria Maso
 */
public class BasePlugInChain implements WebSocketPlugInChain {

	private static final Logger mLog = Logging.getLogger();
	private final List<WebSocketPlugIn> mPlugins = new FastList<WebSocketPlugIn>();
	private WebSocketServer mServer = null;

	/**
	 *
	 * @param aServer
	 */
	public BasePlugInChain(WebSocketServer aServer) {
		mServer = aServer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId() 
					+ "' that engine '" + aEngine.getId() + "' started...");
		}
		try {
			for (WebSocketPlugIn lPlugIn : getPlugIns()) {
				try {
					lPlugIn.engineStarted(aEngine);
				} catch (Exception lEx) {
					mLog.error("Engine '" + aEngine.getId()
							+ "' started at plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage());
				}
			}
		} catch (Exception lEx) {
			mLog.error("Engine '" + aEngine.getId()
					+ "' started: "
					+ lEx.getClass().getSimpleName() + ": "
					+ lEx.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId()
					+ "' that engine '" + aEngine.getId()
					+ "' stopped...");
		}
		try {
			for (WebSocketPlugIn lPlugIn : getPlugIns()) {
				try {
					lPlugIn.engineStopped(aEngine);
				} catch (Exception lEx) {
					mLog.error("Engine '" + aEngine.getId()
							+ "' stopped at plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName()
							+ ": " + lEx.getMessage());
				}
			}
		} catch (Exception lEx) {
			mLog.error("Engine '" + aEngine.getId()
					+ "' stopped: "
					+ lEx.getClass().getSimpleName() + ": "
					+ lEx.getMessage());
		}
	}

	/**
	 * @param aConnector
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId() 
					+ "' that connector '"
					+ aConnector.getId() + "' started...");
		}
		try {
			for (Iterator<WebSocketPlugIn> lIterator = getPlugIns().iterator(); lIterator.hasNext();) {
				WebSocketPlugIn lPlugIn = lIterator.next();
				if (lPlugIn.getEnabled()) {
					try {
						// log.debug("Notifying plug-in " + plugIn + " that connector started...");
						lPlugIn.connectorStarted(aConnector);
					} catch (Exception lEx) {
						mLog.error("Connector '"
								+ aConnector.getId()
								+ "' started at plug-in '"
								+ lPlugIn.getId() + "': "
								+ lEx.getClass().getSimpleName() + ": "
								+ lEx.getMessage());
					}
				}
			}
		} catch (Exception lEx) {
			mLog.error("Connector '"
					+ aConnector.getId() + "' started (2): "
					+ lEx.getClass().getSimpleName() + ": "
					+ lEx.getMessage());
		}
	}

	@Override
	public void sessionStarted(WebSocketConnector aConnector, WebSocketSession aSession) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId() 
					+ "' that client session '"
					+ aSession.getSessionId() + "' started...");
		}
		for (Iterator<WebSocketPlugIn> lIterator = getPlugIns().iterator(); lIterator.hasNext();) {
			WebSocketPlugIn lPlugIn = lIterator.next();
			if (lPlugIn.getEnabled()) {
				try {
					lPlugIn.sessionStarted(aConnector, aSession);
				} catch (Exception lEx) {
					mLog.error("Session '"
							+ aSession.getSessionId()
							+ "' started at plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage());
				}
			}
		}
	}

	@Override
	public PlugInResponse processPacket(WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing packet for plug-ins on connector '" + aConnector.getId() + "'...");
		}
		PlugInResponse lPluginResponse = new PlugInResponse();
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			if (lPlugIn.getEnabled()) {
				try {
					lPlugIn.processPacket(lPluginResponse, aConnector, aDataPacket);
				} catch (Exception lEx) {
					mLog.error("Processing packet at connector '"
							+ aConnector.getId()
							+ "', plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage());
				}
				if (lPluginResponse.isChainAborted()) {
					break;
				}
			}
		}
		return lPluginResponse;
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId() 
					+ "'that connector '" + aConnector.getId() 
					+ "' stopped (" + aCloseReason.name() + ")...");
		}
		for (Iterator<WebSocketPlugIn> lIterator = getPlugIns().iterator(); lIterator.hasNext();) {
			WebSocketPlugIn lPlugIn = lIterator.next();
			if (lPlugIn.getEnabled()) {
				try {
					lPlugIn.connectorStopped(aConnector, aCloseReason);
				} catch (Exception lEx) {
					mLog.error("Connector '"
							+ aConnector.getId()
							+ "' stopped at plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage());
				}
			}
		}
	}

	@Override
	public void sessionStopped(WebSocketSession aSession) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Notifying plug-ins of server '" + getServer().getId() 
					+ "' that client session '"
					+ aSession.getSessionId() + "' stopped...");
		}
		for (Iterator<WebSocketPlugIn> lIterator = getPlugIns().iterator(); lIterator.hasNext();) {
			WebSocketPlugIn lPlugIn = lIterator.next();
			if (lPlugIn.getEnabled()) {
				try {
					lPlugIn.sessionStopped(aSession);
				} catch (Exception lEx) {
					mLog.error("Session '"
							+ aSession.getSessionId()
							+ "' stopped at plug-in '"
							+ lPlugIn.getId() + "': "
							+ lEx.getClass().getSimpleName() + ": "
							+ lEx.getMessage());
				}
			}
		}
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<WebSocketPlugIn> getPlugIns() {
		return mPlugins;
	}

	/**
	 *
	 * @param aPlugIn
	 */
	@Override
	public void addPlugIn(WebSocketPlugIn aPlugIn) {
		mPlugins.add(aPlugIn);
		aPlugIn.setPlugInChain(this);
	}

	/**
	 *
	 * @param aPlugIn
	 */
	@Override
	public void removePlugIn(WebSocketPlugIn aPlugIn) {
		mPlugins.remove(aPlugIn);
		aPlugIn.setPlugInChain(null);
	}

	/**
	 * returns a plug-in identified by the given id.
	 */
	@Override
	public WebSocketPlugIn getPlugIn(String aId) {
		if (aId != null) {
			for (WebSocketPlugIn lPlugIn : mPlugins) {
				if (lPlugIn.getId() != null && aId.equals(lPlugIn.getId())) {
					return lPlugIn;
				}
			}
		}
		return null;
	}

	/**
	 * @return the server
	 */
	@Override
	public WebSocketServer getServer() {
		return mServer;
	}

	@Override
	public void addPlugIn(Integer aPosition, WebSocketPlugIn aPlugIn) {
		mPlugins.add(aPosition, aPlugIn);
		aPlugIn.setPlugInChain(this);
	}

	@Override
	public void systemStarting() throws Exception {
		if (mPlugins.isEmpty()) {
			return;
		}
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			try {
				lPlugIn.systemStarting();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarting' event at plug-in '"
						+ lPlugIn.getId() + "': "
						+ lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
			}
		}
	}

	@Override
	public void systemStarted() throws Exception {
		if (mPlugins.isEmpty()) {
			return;
		}
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			try {
				lPlugIn.systemStarted();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStarted' event at plug-in '"
						+ lPlugIn.getId() + "': "
						+ lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
			}
		}
	}

	@Override
	public void systemStopping() throws Exception {
		if (mPlugins.isEmpty()) {
			return;
		}
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			try {
				lPlugIn.systemStopping();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopping' event at plug-in '"
						+ lPlugIn.getId() + "': "
						+ lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
			}
		}
	}

	@Override
	public void systemStopped() throws Exception {
		if (mPlugins.isEmpty()) {
			return;
		}
		for (WebSocketPlugIn lPlugIn : getPlugIns()) {
			try {
				lPlugIn.systemStopped();
			} catch (Exception lEx) {
				mLog.error("Notifying 'systemStopped' event at plug-in '"
						+ lPlugIn.getId() + "': "
						+ lEx.getClass().getSimpleName() + ": "
						+ lEx.getMessage());
			}
		}
	}
}
