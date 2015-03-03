//	---------------------------------------------------------------------------
//	jWebSocket - ServerClient for Scripting Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.scripting.app;

import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.script.Invocable;
import org.jwebsocket.api.IInternalConnectorListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.connectors.InternalConnector;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.server.InternalClient;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.JWSTimerTask;
import org.jwebsocket.util.Tools;

/**
 * Facade of org.jwebsocket.server.InternalClient for ScriptingPlugIn.
 *
 * @author Rolando Santamaria Maso
 */
public class ServerClient {

	abstract class Operation {

		private InternalClient mClient;

		public Operation(InternalClient aClient) {
			mClient = aClient;
		}
	}

	class OpenOperation extends Operation {

		public OpenOperation(InternalClient aClient) {
			super(aClient);
		}
	}

	class CloseOperation extends Operation {

		public CloseOperation(InternalClient aClient) {
			super(aClient);
		}
	}

	class SendTokenOperation extends Operation {

		private Token mToken;
		private WebSocketResponseTokenListener mListener;

		public SendTokenOperation(InternalClient aClient, Token aToken,
				WebSocketResponseTokenListener aListener) {
			super(aClient);
			mToken = aToken;
			mListener = aListener;
		}
	}

	class SendPacketOperation extends Operation {

		private WebSocketPacket mPacket;

		public SendPacketOperation(InternalClient aClient, WebSocketPacket mPacket) {
			super(aClient);
			this.mPacket = mPacket;
		}
	}
	private final InternalClient mClient;
	private final BaseScriptApp mScriptApp;
	/**
	 * The ServerClient operations that finally invoke internal plug-ins
	 * operations should be called out of the script app calling thread, its
	 * avoid internal plug-ins issues if running into the script app security
	 * sandbox.
	 */
	private static Queue<Operation> mQueuedOperations = new ConcurrentLinkedQueue<Operation>();

	static {
		Tools.getTimer().scheduleAtFixedRate(new JWSTimerTask() {
			@Override
			public void runTask() {
				if (!mQueuedOperations.isEmpty()) {
					Operation lOp = mQueuedOperations.poll();
					if (lOp instanceof SendPacketOperation) {
						lOp.mClient.sendPacket(((SendPacketOperation) lOp).mPacket);
					} else if (lOp instanceof SendTokenOperation) {
						lOp.mClient.sendToken(((SendTokenOperation) lOp).mToken,
								((SendTokenOperation) lOp).mListener);
					} else if (lOp instanceof OpenOperation) {
						lOp.mClient.open();
					} else if (lOp instanceof CloseOperation) {
						lOp.mClient.close();
					}
				}
			}
		}, 0, 50);
	}

	/**
	 *
	 * @param aScriptApp
	 */
	public ServerClient(BaseScriptApp aScriptApp) {
		mClient = new InternalClient();
		mScriptApp = aScriptApp;
	}

	/**
	 *
	 * @return
	 */
	public String getId() {
		return mClient.getId();
	}

	/**
	 *
	 * @return
	 */
	public String getUsername() {
		return mClient.getUsername();
	}

	/**
	 *
	 * @return
	 */
	public InternalConnector getConnector() {
		return mClient.getConnector();
	}

	/**
	 *
	 * @return
	 */
	public boolean isConnected() {
		return mClient.isConnected();
	}

	/**
	 *
	 */
	public void open() {
		if (!isConnected()) {
			mQueuedOperations.add(new OpenOperation(mClient));
		}
	}

	/**
	 *
	 * @param aPacket
	 */
	public void sendPacket(String aPacket) {
		sendPacket(new RawPacket(aPacket));
	}

	/**
	 *
	 * @param aPacket
	 */
	public void sendPacket(WebSocketPacket aPacket) {
		mQueuedOperations.add(new SendPacketOperation(mClient, aPacket));
	}

	/**
	 *
	 * @param aToken
	 */
	public void sendToken(Map aToken) {
		Token lToken = TokenFactory.createToken(aToken);
		mClient.sendToken(lToken);
	}

	/**
	 *
	 * @param aToken
	 * @param aListener
	 */
	public void sendToken(Map aToken, Object aListener) {
		final WebSocketResponseTokenListener lListener = (WebSocketResponseTokenListener) ((Invocable) mScriptApp.getEngine())
				.getInterface(aListener, WebSocketResponseTokenListener.class);

		// wrap listener 'OnTimeout' callback to use sandbox restrictions
		mQueuedOperations.add(new SendTokenOperation(mClient, TokenFactory.createToken(aToken),
				new WebSocketResponseTokenListener() {
					@Override
					public long getTimeout() {
						return (Long) Tools.doPrivileged(mScriptApp.getPermissions(), new PrivilegedAction<Long>() {
							@Override
							public Long run() {
								return lListener.getTimeout();
							}
						});
					}

					@Override
					public void setTimeout(long aTimeout) {
					}

					@Override
					public void OnTimeout(final Token aToken) {
						Tools.doPrivileged(mScriptApp.getPermissions(), new PrivilegedAction<Object>() {
							@Override
							public Object run() {
								lListener.OnTimeout(aToken);
								return null;
							}
						});
					}

					@Override
					public void OnResponse(final Token aToken) {
						lListener.OnResponse(aToken);
					}

					@Override
					public void OnSuccess(final Token aToken) {
						lListener.OnSuccess(aToken);
					}

					@Override
					public void OnFailure(final Token aToken) {
						lListener.OnFailure(aToken);
					}
				}));
	}

	/**
	 *
	 * @param aListener
	 * @return
	 */
	public IInternalConnectorListener addListener(Object aListener) {
		return mClient.addListener((IInternalConnectorListener) ((Invocable) mScriptApp.getEngine())
				.getInterface(aListener, IInternalConnectorListener.class));
	}

	/**
	 *
	 * @param aListener
	 */
	public void removeListener(IInternalConnectorListener aListener) {
		mClient.removeListener(aListener);
	}

	/**
	 *
	 */
	public void close() {
		if (isConnected()) {
			mQueuedOperations.add(new CloseOperation(mClient));
		}
	}
}
