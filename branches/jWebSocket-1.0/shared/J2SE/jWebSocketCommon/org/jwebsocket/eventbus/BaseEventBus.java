//	---------------------------------------------------------------------------
//	jWebSocket BaseEventBus (Community Edition, CE)
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
package org.jwebsocket.eventbus;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jwebsocket.api.IEventBus;
import org.jwebsocket.api.IInitializable;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public abstract class BaseEventBus implements IEventBus, IInitializable {

	private final Map<String, IHandler> mResponseHandlers = new FastMap<String, IHandler>().shared();
	private final Map<String, List<IHandler>> mHandlers = new FastMap<String, List<IHandler>>().shared();

	/**
	 * Send a token
	 *
	 * @param aToken
	 * @return
	 */
	@Override
	public IEventBus send(Token aToken) {
		return send(aToken, null);
	}

	IHandler removeResponseHandler(String aTokenUID) {
		return mResponseHandlers.remove(aTokenUID);
	}

	void storeResponseHandler(String aTokenUID, IHandler aHandler) {
		mResponseHandlers.put(aTokenUID, aHandler);
	}

	void removeHandler(String aNS, IHandler aHandler) {
		if (mHandlers.containsKey(aNS)) {
			mHandlers.get(aNS).remove(aHandler);
		}
	}

	synchronized void storeHandler(String aNS, IHandler aHandler) {
		if (!mHandlers.containsKey(aNS)) {
			mHandlers.put(aNS, new FastList<IHandler>());
		}

		mHandlers.get(aNS).add(aHandler);
	}

	void invokeHandlers(String aNS, final Token aToken) {
		for (String lNS : mHandlers.keySet()) {
			if ((!aNS.matches(lNS) && !Tools.wildCardMatch(aNS, lNS)) || mHandlers.get(lNS).isEmpty()) {
				continue;
			}

			List<IHandler> lHandlers = mHandlers.get(lNS);
			final IEventBus lEB = this;
			for (final IHandler lH : lHandlers) {
				Tools.getThreadPool().submit(new Runnable() {

					@Override
					public void run() {
						lH.setEventBus(lEB);
						lH.OnMessage(aToken);
					}
				});
			}
		}
	}

	void invokeHandler(String aNS, final Token aToken) {
		for (String lNS : mHandlers.keySet()) {
			if (!aNS.matches(lNS) || !Tools.wildCardMatch(aNS, lNS) || mHandlers.get(lNS).isEmpty()) {
				continue;
			}

			final IHandler lH = mHandlers.get(lNS).get(0);
			final IEventBus lEB = this;
			Tools.getThreadPool().submit(new Runnable() {

				@Override
				public void run() {
					lH.setEventBus(lEB);
					lH.OnMessage(aToken);
				}
			});

			return;
		}
	}

	void invokeResponseHandler(String aTokenUID, final Token aResponse) {
		final IHandler lH = removeResponseHandler(aTokenUID);
		if (null != lH) {
			Tools.getThreadPool().submit(new Runnable() {

				@Override
				public void run() {
					lH.OnMessage(aResponse);

					// supporting jWebSocket callbacks style
					lH.OnResponse(aResponse);

					if (Handler.STATUS_OK.equals(aResponse.getCode())) {
						lH.OnSuccess(aResponse);
					} else {
						lH.OnFailure(aResponse);
					}
				}
			});
		}
	}
}
