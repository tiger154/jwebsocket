//	---------------------------------------------------------------------------
//	jWebSocket - TokenPlugIn (Convenience Class) (Community Edition, CE)
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
import javax.jms.MapMessage;
import org.jwebsocket.api.IChunkable;
import org.jwebsocket.api.IChunkableDeliveryListener;
import org.jwebsocket.api.IEmbeddedAuthentication;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.jms.JMSServer;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.system.SecurityHelper;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.JMSManager;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 *
 * @author Alexander Schulze
 * @author Marcos Antonio Gonzalez Huerta
 * @author Rolando Santamaria Maso
 */
public class TokenPlugIn extends BasePlugIn {

	private String mNamespace = null;

	/**
	 *
	 * @param aConnector
	 */
	public void processLogon(WebSocketConnector aConnector) {
	}

	/**
	 *
	 * @param aConnector
	 */
	public void processLogoff(WebSocketConnector aConnector) {
	}

	/**
	 *
	 * @param aConfiguration
	 */
	public TokenPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
	}

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
	}

	/**
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
	}

	/**
	 * @param aConnector
	 * @param aToken
	 *
	 * @return
	 */
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @return
	 */
	public List<String> invokeMethodList() {
		return null;
	}

	@Override
	public void processPacket(PlugInResponse aResponse, WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
		//
	}

	/**
	 *
	 * @param aConnector
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
	}

	/**
	 * @return the namespace
	 */
	@Override
	public String getNamespace() {
		return mNamespace;
	}

	/**
	 * @param aNamespace the namespace to set
	 */
	public void setNamespace(String aNamespace) {
		this.mNamespace = aNamespace;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public TokenServer getServer() {
		return (TokenServer) super.getServer();
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createResponse</tt> to simplify token plug-in code.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createResponse(Token aInToken) {
		TokenServer lServer = getServer();
		if (lServer != null) {
			return lServer.createResponse(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>createAccessDenied</tt> to simplify token plug-in code.
	 *
	 * @param aInToken
	 * @return
	 */
	public Token createAccessDenied(Token aInToken) {
		TokenServer lServer = getServer();
		if (lServer != null) {
			return lServer.createAccessDenied(aInToken);
		} else {
			return null;
		}
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendToken(aSource, aTarget, aToken);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendToken(aTarget, aToken);
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aListener
	 */
	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			IPacketDeliveryListener aListener) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenInTransaction(aTarget, aToken, aListener);
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aFragmentSize
	 */
	public void sendTokenFragmented(WebSocketConnector aTarget, Token aToken, int aFragmentSize) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenFragmented(aTarget, aToken, aFragmentSize);
	}

	/**
	 *
	 * @param aTarget
	 * @param aToken
	 * @param aFragmentSize
	 * @param aListener
	 */
	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			int aFragmentSize, IPacketDeliveryListener aListener) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenInTransaction(aTarget, aToken, aFragmentSize, aListener);
	}

	/**
	 *
	 * @param aConnector
	 * @param aChunkable
	 */
	public void sendChunkable(WebSocketConnector aConnector, IChunkable aChunkable) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendChunkable(aConnector, aChunkable);
	}

	/**
	 *
	 * @param aConnector
	 * @param aChunkable
	 * @param aListener
	 */
	public void sendChunkable(WebSocketConnector aConnector, IChunkable aChunkable,
			IChunkableDeliveryListener aListener) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendChunkable(aConnector, aChunkable, aListener);
	}

	/**
	 * Sends the the given token asynchronously and returns the future object to
	 * keep track of the send operation
	 *
	 * @param aSource the source connector
	 * @param aTarget the target connector
	 * @param aToken the token object
	 * @return the I/O future object
	 */
	public IOFuture sendTokenAsync(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		return lServer.sendTokenAsync(aSource, aTarget, aToken);
	}

	/**
	 * Convenience method, just a wrapper for token server method
	 * <tt>sendToken</tt> to simplify token plug-in code.
	 *
	 * @param aSource
	 * @param aToken
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken) {
		Assert.notNull(getServer(), "Token server reference cannot be null!");

		getServer().broadcastToken(aSource, aToken);
	}

	/**
	 *
	 * @param aSource
	 * @param aToken
	 * @param aBroadcastOptions
	 */
	public void broadcastToken(WebSocketConnector aSource, Token aToken, BroadcastOptions aBroadcastOptions) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.broadcastToken(aSource, aToken, aBroadcastOptions);
	}

	/**
	 *
	 * @param aConnector
	 * @param aInToken
	 * @param aErrCode
	 * @param aMessage
	 */
	public void sendErrorToken(WebSocketConnector aConnector, Token aInToken,
			int aErrCode, String aMessage) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendErrorToken(aConnector, aInToken, aErrCode, aMessage);
	}

	/**
	 *
	 * @param aResponse
	 * @param aType
	 * @param aVersion
	 * @param aReason
	 */
	public void createReasonOfChange(Token aResponse, ChangeType aType, String aVersion, String aReason) {
		aResponse.setNS(getNamespace());
		aResponse.setType("processChangeOfPlugIn");
		aResponse.setString("changeType", aType.toString());
		aResponse.setString("version", aVersion);
		aResponse.setString("reason", aReason);
		aResponse.setString("id", getId());
	}

	/**
	 *
	 * @return The global jWebSocket application context (spring beans)
	 */
	public ApplicationContext getConfigBeanFactory() {
		String lSpringConfig = getString("spring_config");
		if (null == lSpringConfig || lSpringConfig.isEmpty()) {
			return null;
		}
		JWebSocketBeanFactory.load(lSpringConfig, getClass().getClassLoader());
		return JWebSocketBeanFactory.getInstance();
	}

	/**
	 *
	 * @param aNamespace
	 * @return An named application context (spring beans)
	 */
	public ApplicationContext getConfigBeanFactory(String aNamespace) {
		String lSpringConfig = getString("spring_config");
		if (null == lSpringConfig || lSpringConfig.isEmpty()) {
			return null;
		}
		JWebSocketBeanFactory.load(aNamespace, lSpringConfig, getClass().getClassLoader());
		return JWebSocketBeanFactory.getInstance(aNamespace);
	}

	/**
	 *
	 * @param aConnector
	 * @param aAuthority
	 * @return
	 */
	public boolean hasAuthority(WebSocketConnector aConnector, String aAuthority) {
		String lAuthenticationMethod = getAuthenticationMethod();

		if (lAuthenticationMethod.equals(AUTHENTICATION_METHOD_SPRING)) {
			return SecurityHelper.userHasAuthority(aConnector, aAuthority);
		} else if (lAuthenticationMethod.equals(AUTHENTICATION_METHOD_EMBEDDED)) {
			if (aConnector instanceof IEmbeddedAuthentication) {
				return ((IEmbeddedAuthentication) aConnector).hasAuthority(aAuthority);
			} else {
				throw new UnsupportedOperationException("The connector does not "
						+ "implements 'org.jwebsocket.api.IEmbeddedAuthentication'!");
			}
		}
		// authentication method not supported
		throw new UnsupportedOperationException("Unsupported authentication method. "
				+ "Supported methods are: spring or embedded!");
	}

	/**
	 * Get a client configuration parameter for this plug-in.
	 *
	 * @param aConnector
	 * @param aKey The parameter name
	 * @return
	 */
	public Object getConfigParam(WebSocketConnector aConnector, String aKey) {
		return getConfigParam(aConnector, aKey, null);
	}

	/**
	 * Get a client configuration parameter for this plug-in.
	 *
	 * @param aConnector
	 * @param aKey The parameter name
	 * @param aDefaultValue A default value if the parameter value is null
	 * @return
	 */
	public Object getConfigParam(WebSocketConnector aConnector, String aKey, Object aDefaultValue) {
		if (null == aConnector.getSession().getStorage()) {
			return aDefaultValue;
		}

		Object lValue = aConnector.getSession().getStorage().get(getNamespace() + "." + aKey);

		if (null == lValue) {
			return aDefaultValue;
		}

		return lValue;
	}

	/**
	 * Invoke a feature on a target plug-in by using the plug-in's 'invoke'
	 * interface
	 *
	 * @param aPlugInId
	 * @param aConnector
	 * @param aToken
	 * @return
	 */
	public Token invokePlugIn(String aPlugInId, WebSocketConnector aConnector, Token aToken) {
		// getting the plug-in instance
		TokenPlugIn lPlugIn = (TokenPlugIn) getPlugInChain().getPlugIn(aPlugInId);
		Assert.notNull(lPlugIn, "The target plug-in is not running!");
		// setting the tokan namespace if missing
		aToken.setNS(lPlugIn.getNamespace());

		return lPlugIn.invoke(aConnector, aToken);
	}

	/**
	 * Send a message through the server JMSManager instance that notifies that
	 * a given incoming Token has been processed.
	 *
	 * @param aUsername The calling username
	 * @param aInToken The processed Token
	 * @param aCode The processing result code
	 * @throws java.lang.Exception
	 */
	public void notifyProcessed(String aUsername, Token aInToken, Integer aCode) throws Exception {
		// getting the message hub
		JMSManager lMessageHub = getServer().getJMSManager();

		// creating the message to be sent
		MapMessage lMsg = lMessageHub.buildMessage(JWebSocketServerConstants.NS_BASE + ".plugins", "tokenProcessed");
		lMsg.setStringProperty("tokenNS", aInToken.getNS());
		lMsg.setStringProperty("tokenType", aInToken.getType());
		lMsg.setStringProperty("username", aUsername);
		lMsg.setStringProperty("nodeId", JWebSocketConfig.getConfig().getNodeId());
		lMsg.setIntProperty("code", aCode);

		// sending the message
		lMessageHub.send(lMsg);

		// listening the sent message 
//		lMessageHub.subscribe(new MessageListener() {
//			@Override
//			public void onMessage(Message msg) {
//				MapMessage lMessage = (MapMessage) msg;
//
//				try {
//					String lUsername = lMessage.getStringProperty("username");
//					String lNS = lMessage.getStringProperty("tokenNS");
//					String lType = lMessage.getStringProperty("tokenType");
//					
//					// TODO: call your logic here
//				} catch (Exception lEx) {
//					// catch here
//				}
//			}
//		}, "ns = 'org.jwebsocket.plugins' AND msgType='tokenProcessed'");
	}

	/**
	 * Return TRUE if the jWebSocket cluster is enabled, FALSE otherwise
	 *
	 * @return
	 */
	public boolean isClusterEnabled() {
		return getServer() instanceof JMSServer;
	}
}
