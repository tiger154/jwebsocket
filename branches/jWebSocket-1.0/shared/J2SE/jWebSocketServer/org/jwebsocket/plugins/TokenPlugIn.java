//	---------------------------------------------------------------------------
//	jWebSocket - Wrapper for Token based PlugIns (Convenience Class)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.plugins;

import java.util.List;
import org.jwebsocket.api.IChunkable;
import org.jwebsocket.api.IChunkableDeliveryListener;
import org.jwebsocket.api.IEmbeddedAuthentication;
import org.jwebsocket.api.IPacketDeliveryListener;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.ChangeType;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.system.SecurityHelper;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.token.Token;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 *
 * @author aschulze
 * @author Marcos Antonio González Huerta (markos0886, UCI)
 * @author kyberneees
 */
public class TokenPlugIn extends BasePlugIn {

	private String mNamespace = null;

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
	 * @param aSource
	 * @param aTarget
	 * @param aToken
	 */
	public void sendToken(WebSocketConnector aTarget, Token aToken) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendToken(aTarget, aToken);
	}

	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			IPacketDeliveryListener aListener) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenInTransaction(aTarget, aToken, aListener);
	}

	public void sendTokenFragmented(WebSocketConnector aTarget, Token aToken, int aFragmentSize) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenFragmented(aTarget, aToken, aFragmentSize);
	}

	public void sendTokenInTransaction(WebSocketConnector aTarget, Token aToken,
			int aFragmentSize, IPacketDeliveryListener aListener) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendTokenInTransaction(aTarget, aToken, aFragmentSize, aListener);
	}

	public void sendChunkable(WebSocketConnector aConnector, IChunkable aChunkable) {
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.sendChunkable(aConnector, aChunkable);
	}

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
		TokenServer lServer = getServer();
		Assert.notNull(lServer, "Token server reference cannot be null!");

		lServer.broadcastToken(aSource, aToken);
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
	 * @return An named application context (spring beans)
	 */
	public ApplicationContext getConfigBeanFactory(String aNamespace) {
		String lSpringConfig = getString("spring_config");
		if (null == lSpringConfig || lSpringConfig.isEmpty()) {
			return null;
		}
		JWebSocketBeanFactory.load(lSpringConfig, getClass().getClassLoader());
		return JWebSocketBeanFactory.getInstance(aNamespace);
	}

	public boolean hasAuthority(WebSocketConnector aConnector, String aAuthority) {
		String lAuthenticationMethod = getAuthenticationMethod();

		if (lAuthenticationMethod.equals(AUTHENTICATION_METHOD_STATIC)) {
			return SecurityFactory.hasRight(aConnector.getUsername(), aAuthority);
		} else if (lAuthenticationMethod.equals(AUTHENTICATION_METHOD_SPRING)) {
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
				+ "Supported methods are: spring, static or embedded!");
	}
}
