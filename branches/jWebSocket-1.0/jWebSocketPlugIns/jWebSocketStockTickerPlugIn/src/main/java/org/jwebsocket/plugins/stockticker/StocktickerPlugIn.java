//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket StockTicker Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.stockticker;

import org.jwebsocket.plugins.stockticker.Implementation.User;
import org.jwebsocket.plugins.stockticker.event.Buy;
import org.jwebsocket.plugins.stockticker.event.Chart;
import org.jwebsocket.plugins.stockticker.event.Combobox;
import org.jwebsocket.plugins.stockticker.event.CreateUser;
import org.jwebsocket.plugins.stockticker.event.Login;
import org.jwebsocket.plugins.stockticker.event.GetTickets;
import org.jwebsocket.plugins.stockticker.event.Logout;
import org.jwebsocket.plugins.stockticker.event.ReadBuy;
import org.jwebsocket.plugins.stockticker.event.Sell;
import org.jwebsocket.plugins.stockticker.service.StocktickerService;
import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.eventmodel.plugin.EventModelPlugIn;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.event.C2SResponseEvent;
import org.jwebsocket.eventmodel.event.em.EngineStopped;
import org.jwebsocket.eventmodel.observable.ResponseEvent;

/**
 *
 * @author Roy
 */
public class StocktickerPlugIn extends EventModelPlugIn {

	static Logger mLog = Logging.getLogger(StocktickerPlugIn.class);
	private final StocktickerService mService;
	private Thread mStocktickerThread;
	private boolean mThreadStarted = false;
	private final List<WebSocketConnector> mClients = new FastList<WebSocketConnector>();

	/**
	 *
	 */
	public StocktickerPlugIn() {
		mService = new StocktickerService();
	}

	/**
	 *
	 * @return
	 */
	public List<WebSocketConnector> getClients() {
		return mClients;
	}

	@Override
	public void initialize() throws Exception {
		mService.setPlugIn(this);
		mStocktickerThread = new Thread(mService);
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(CreateUser aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing CreateUser event notification...");
		}
		Boolean val = mService.createUser(new User(aEvent.getUser(), aEvent.getPass()));

		if (val) {
			aResponseEvent.getArgs().setBoolean("success", true);
			aResponseEvent.getArgs().setInteger("code", 0);

		} else {
			aResponseEvent.getArgs().setBoolean("success", false);
			aResponseEvent.getArgs().setInteger("code", -1);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Login aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Login event notification...");
		}
		aEvent.getConnector().setVar("userlogin", aEvent.getUser());
		if (mService.login(new User(aEvent.getUser(), aEvent.getPass()))) {
			aResponseEvent.getArgs().setInteger("code", 0);
			aResponseEvent.getArgs().setBoolean("success", true);
		} else {
			aResponseEvent.getArgs().setInteger("code", -1);
			aResponseEvent.getArgs().setBoolean("failure", true);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(GetTickets aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing GetTickets event notification...");
		}
		//Registering clients 
		if (!mClients.contains(aEvent.getConnector())) {
			mClients.add(aEvent.getConnector());
		}
		if (!mThreadStarted) {
			//Sending data every 1 second
			mStocktickerThread.start();
			mThreadStarted = true;
		}
		aResponseEvent.getArgs().setList("data", mService.listRecords());
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Buy aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Buy event notification...");
		}
		Boolean val = mService.buy(aEvent.getName(), aEvent.getCant(), (String) aEvent.getConnector().getVar("userlogin"));
		if (val) {
			aResponseEvent.getArgs().setBoolean("success", true);
			aResponseEvent.getArgs().setInteger("code", 0);
		} else {
			aResponseEvent.getArgs().setBoolean("success", false);
			aResponseEvent.getArgs().setInteger("code", -1);
		}

	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(ReadBuy aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing ReadBuy event notification...");
		}
		aResponseEvent.getArgs().setList("data", mService.readBuy((String) aEvent.getConnector().getVar("userlogin")));
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Sell aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Sell event notification...");
		}
		Boolean val = mService.sell(aEvent.getName(), aEvent.getCant(), (String) aEvent.getConnector().getVar("userlogin"));
		if (val) {
			aResponseEvent.getArgs().setBoolean("success", true);
			aResponseEvent.getArgs().setInteger("code", 0);
		} else {
			aResponseEvent.getArgs().setBoolean("success", false);
			aResponseEvent.getArgs().setInteger("code", -1);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Combobox aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Combox event notification...");
		}
		aResponseEvent.getArgs().setList("data", (List<String>) mService.showComb((String) aEvent.getConnector().getVar("userlogin")));
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Chart aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Chart event notification...");
		}
		aResponseEvent.getArgs().setInteger("data", mService.chart((String) aEvent.getConnector().getVar("userlogin"), aEvent.getNamechart()));
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(Logout aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Logout event notification...");
		}
		if (mClients.contains(aEvent.getConnector())) {
			mClients.remove(aEvent.getConnector());
			aResponseEvent.getArgs().setBoolean("success", true);
			aResponseEvent.getArgs().setInteger("code", 0);
		} else {
			aResponseEvent.getArgs().setBoolean("success", false);
			aResponseEvent.getArgs().setInteger("code", -1);
		}
	}

	/**
	 *
	 * @param aEvent
	 * @param aResponseEvent
	 */
	public void processEvent(EngineStopped aEvent, ResponseEvent aResponseEvent) {
		mThreadStarted = false;
		try {
			mStocktickerThread.join(200);
			mStocktickerThread.stop();
		} catch (InterruptedException ex) {
			mLog.error(ex.getMessage());
		}
	}
}
