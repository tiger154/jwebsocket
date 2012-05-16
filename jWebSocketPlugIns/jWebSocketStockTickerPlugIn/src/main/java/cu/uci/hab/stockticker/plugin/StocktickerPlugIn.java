/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin;

import cu.uci.hab.stockticker.plugin.Implementation.User;
import cu.uci.hab.stockticker.plugin.event.Buy;
import cu.uci.hab.stockticker.plugin.event.Chart;
import cu.uci.hab.stockticker.plugin.event.Combobox;
import cu.uci.hab.stockticker.plugin.event.CreateUser;
import cu.uci.hab.stockticker.plugin.event.Login;
import cu.uci.hab.stockticker.plugin.event.GetTickets;
import cu.uci.hab.stockticker.plugin.event.Logout;
import cu.uci.hab.stockticker.plugin.event.ReadBuy;
import cu.uci.hab.stockticker.plugin.event.Sell;
import cu.uci.hab.stockticker.plugin.service.StocktickerService;
import java.util.List;
import java.util.logging.Level;
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
	private StocktickerService mService;
	private Thread mStocktickerThread;
	private boolean mThreadStarted = false;
	private List<WebSocketConnector> mClients = new FastList<WebSocketConnector>();

	public StocktickerPlugIn() {
		mService = new StocktickerService();
	}

	public List<WebSocketConnector> getClients() {
		return mClients;
	}

	@Override
	public void initialize() throws Exception {
		mService.setPlugIn(this);
		mStocktickerThread = new Thread(mService);
	}

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

	public void processEvent(ReadBuy aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing ReadBuy event notification...");
		}
		aResponseEvent.getArgs().setList("data", mService.readBuy((String) aEvent.getConnector().getVar("userlogin")));
	}

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

	public void processEvent(Combobox aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Combox event notification...");
		}
		aResponseEvent.getArgs().setList("data", (List<String>) mService.showComb((String) aEvent.getConnector().getVar("userlogin")));
	}

	public void processEvent(Chart aEvent, C2SResponseEvent aResponseEvent) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing Chart event notification...");
		}
		aResponseEvent.getArgs().setInteger("data", mService.chart((String) aEvent.getConnector().getVar("userlogin"), aEvent.getNamechart()));
	}

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
