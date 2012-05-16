/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import cu.uci.hab.stockticker.plugin.Implementation.Purchasing;
import cu.uci.hab.stockticker.plugin.Implementation.Record;
import cu.uci.hab.stockticker.plugin.StocktickerPlugIn;
import cu.uci.hab.stockticker.plugin.api.IPurchasing;
import cu.uci.hab.stockticker.plugin.api.IRecord;
import cu.uci.hab.stockticker.plugin.api.IService;
import cu.uci.hab.stockticker.plugin.api.IUser;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Roy
 */
public class StocktickerService extends Thread implements IService {

	private Mongo mConnection;
	private DBCollection mCollectionUser;
	private DBCollection mCollectionTicket;
	private DBCollection mCollectionPurchasing;
	private StocktickerPlugIn mPlugIn;

	public StocktickerPlugIn getPlugIn() {
		return mPlugIn;
	}

	public void setPlugIn(StocktickerPlugIn aPlugIn) {
		this.mPlugIn = aPlugIn;
	}

	public StocktickerService() {
		try {
			mConnection = new Mongo();
			mCollectionUser = mConnection.getDB("StockTicker").
					getCollection("Users");
			mCollectionTicket = mConnection.getDB("StockTicker").
					getCollection("Tickets");
			mCollectionPurchasing = mConnection.getDB("StockTicker").
					getCollection("Purchasing");
		} catch (UnknownHostException ex) {
			Logger.getLogger(StocktickerService.class.getName()).
					log(Level.SEVERE, null, ex);
		} catch (MongoException ex) {
			Logger.getLogger(StocktickerService.class.getName()).
					log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(StocktickerService.class.getName()).
					log(Level.SEVERE, null, ex);
		}
	}

	/**
	 *
	 * @param aUser users instance that is registering.
	 * @return Boolean true if the user is, false if the user is not.
	 */
	@Override
	public Boolean createUser(IUser aUser) {
		BasicDBObject lUsers = new BasicDBObject();
		DBObject lUser = mCollectionUser.findOne(
				new BasicDBObject("user", aUser.getUser()));
		if (lUser == null) {
			lUsers.put("user", aUser.getUser());
			lUsers.put("pass", aUser.getPass());
			mCollectionUser.insert(lUsers);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param aUser users instance that is login.
	 * @return true if the user and password are, false if the user and password
	 * are not.
	 */
	@Override
	public Boolean login(IUser aUser) {
		DBObject lObject = mCollectionUser.findOne(new BasicDBObject().append("user", aUser.getUser()).
				append("pass", aUser.getPass()));
		if (lObject == null) {
			return false;
		} else {
			return true;
		}
	}

	private double calcRandom(double N, double M) {
		return Math.random() * (N - M) + M;
	}

	private int calcRandomInt(int N, int M) {
		return (int) Math.floor(Math.random() * (N - M + 1) + M);
	}

	/**
	 *
	 * @param aId
	 * @param aName
	 * @return
	 */
	private BasicDBObject valuesOfRecords(Integer aId, String aName) {
		BasicDBObject lRecord = new BasicDBObject();
		List<Double> lHistory = (List<Double>) mCollectionTicket.findOne(new BasicDBObject().append("id", aId)).get("history");

		double ask = Math.rint((calcRandom(80, 140)) * 100) / 100;
		double bid = Math.rint((calcRandom(ask - (ask * 5 / 100), ask)) * 100) / 100;
		double price = Math.rint((calcRandom(bid, ask)) * 100) / 100;
		double chng = (100 * (lHistory.get(0) - price)) / price;
		Integer trend = 2;
		if (chng > 0) {
			trend = 0;
		} else if (chng < 0) {
			trend = 1;
		}
		lHistory.add(0, Math.rint((price) * 100) / 100);
		if (lHistory.size() > 10) {
			lHistory.remove(10);
		}
		lRecord.put("id", aId);
		lRecord.put("name", aName);
		lRecord.put("bid", bid);
		lRecord.put("price", price);
		lRecord.put("ask", ask);
		lRecord.put("chng", Math.rint((chng / 100) * 100) / 100);
		lRecord.put("trend", trend);
		lRecord.put("history", lHistory);
		return lRecord;
	}

	@Override
	public void run() {
		while (true) {
			try {
				int time = (int) Math.floor(Math.random() * 2 + 1);
				Thread.sleep(500 * time);
				int lval = calcRandomInt(1, 6);
				for (int i = 1; i < lval; i++) {
					int ltemp = (int) Math.floor(Math.random() * 10 + 1);
					mCollectionTicket.update(
							new BasicDBObject().append("id", ltemp),
							valuesOfRecords(ltemp, mCollectionTicket.findOne(
							new BasicDBObject().append("id", ltemp)).get("name").toString()));
				}
				Token lToken = TokenFactory.createToken("stockticker", "records");
				lToken.setList("data", listRecords());
				for (WebSocketConnector lClient : mPlugIn.getClients()) {

					mPlugIn.getEm().getParent().getServer().sendToken(lClient, lToken);
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(StocktickerService.class.getName()).
						log(Level.SEVERE, null, ex);
			}

		}
	}

	/**
	 *
	 * @return listRecords List of items stock ticker
	 */
	@Override
	public List<IRecord> listRecords() {
		List<IRecord> lRecords = new FastList<IRecord>();
		IRecord lRecord;
		DBObject lDocument;
		DBCursor lCursor = mCollectionTicket.find();
		while (lCursor.hasNext()) {
			lDocument = lCursor.next();
			lRecord = new Record((Integer.valueOf(lDocument.get("id").toString())),
					(lDocument.get("name").toString()),
					(Double.valueOf(lDocument.get("bid").toString())),
					(Double.valueOf(lDocument.get("price").toString())),
					(Double.valueOf(lDocument.get("ask").toString())),
					(Double.valueOf(lDocument.get("chng").toString())),
					(Integer.valueOf(lDocument.get("trend").toString())),
					(List<Double>) lDocument.get("history"));

			lRecords.add(lRecord);
		}
		return lRecords;
	}

	/**
	 *
	 * @param aName name of the instrument to buy
	 * @param aCant amount of the instrument to buy
	 * @param aUserLogin User buying
	 * @return true if the amount is less than 100, false if the amount is
	 * greater than 100
	 */
	@Override
	public Boolean buy(String aName, String aCant, String aUserLogin) {

		DBObject lBuy = mCollectionPurchasing.findOne(
				new BasicDBObject("user", aUserLogin).append("name", aName));
		try {
			if (lBuy != null) {
				Integer lCant = Integer.valueOf(
						lBuy.get("cant").toString()) + Integer.parseInt(aCant);
				if (lCant <= 100) {
					Double lInversionTicker = Double.valueOf((mCollectionTicket.findOne(
							new BasicDBObject("name", aName)).get("price").toString()));
					List<Double> lHistory = (List<Double>) (mCollectionTicket.findOne(
							new BasicDBObject().append("name", aName)).get("history"));
					Double lFirstHistory = lHistory.get(1);

					Double lInversion = Double.valueOf(lBuy.get("inversion").toString()) + (lInversionTicker * Integer.parseInt(aCant));

					Double lValue = Double.valueOf(lBuy.get("value").toString()) + (lFirstHistory * Integer.parseInt(aCant));
					mCollectionPurchasing.update(lBuy,
							new BasicDBObject("$set",
							new BasicDBObject("cant", lCant).append(
							"inversion", lInversion).append("value", lValue)));
					return true;
				}
				return false;
			} else {
				Double lInversionTicker = Double.valueOf((mCollectionTicket.findOne(
						new BasicDBObject("name", aName)).get("price").toString()));
				List<Double> lHistory = (List<Double>) (mCollectionTicket.findOne(
						new BasicDBObject().append("name", aName)).get("history"));
				Double lFirstHistory = lHistory.get(1);
				mCollectionPurchasing.insert(
						new BasicDBObject("user", aUserLogin).append("name", aName).
						append("cant", aCant).
						append("inversion", Integer.valueOf(aCant) * lInversionTicker).
						append("value", Integer.valueOf(aCant) * lFirstHistory));
				return true;
			}
		} catch (Exception ex) {
			return false;
		}

	}

	/**
	 *
	 * @param aName name of the instrument to sell
	 * @param aCant amount of the instrument to sell
	 * @param aUserLogin User selling
	 * @return true if the amount is less than the amount stored , false if the
	 * amount is greater than the amount stored
	 */
	@Override
	public Boolean sell(String aName, String aCant, String aUserLogin) {
		DBObject lDocument = mCollectionPurchasing.findOne(
				new BasicDBObject("user", aUserLogin).append("name", aName));
		Integer lCant = Integer.valueOf(lDocument.get("cant").toString());
		Double lInv = Double.valueOf(lDocument.get("inversion").toString());
		Double lVal = Double.valueOf(lDocument.get("value").toString());
		Double lInversionTicker = Double.valueOf((mCollectionTicket.findOne(
				new BasicDBObject("name", aName)).get("price").toString()));
		Boolean lResult;
		if (lCant == Integer.valueOf(aCant)) {
			mCollectionPurchasing.remove(lDocument);
			lResult = true;
		} else if (lCant > Integer.valueOf(aCant)) {
			mCollectionPurchasing.update(lDocument,
					new BasicDBObject("$set",
					new BasicDBObject("cant", lCant - Integer.valueOf(aCant)).append("inversion", lInv - (Integer.valueOf(aCant) * lInversionTicker)).
					append("value", lVal - (Integer.valueOf(aCant) * lInversionTicker))));
			lResult = true;
		} else {
			lResult = false;
		}
		return lResult;
	}

	/**
	 *
	 * @param aUser user application
	 * @return List<IPurchasing> list of purchasing for user
	 */
	@Override
	public List<IPurchasing> readBuy(String aUser) {
		List<IPurchasing> lBuys = new FastList<IPurchasing>();
		IPurchasing lBuy;
		DBObject lDocument;
		DBCursor lCursor = mCollectionPurchasing.find(
				new BasicDBObject("user", aUser));
		while (lCursor.hasNext()) {
			lDocument = lCursor.next();
			lBuy = new Purchasing(((lDocument.get("user").toString())),
					(lDocument.get("name").toString()),
					(Integer.valueOf(lDocument.get("cant").toString())),
					(Double.valueOf(lDocument.get("inversion").toString())),
					(Double.valueOf(lDocument.get("value").toString())));
			lBuys.add(lBuy);
		}
		return lBuys;
	}

	/**
	 *
	 * @param aUser user application
	 * @return List<String> list of names of the instruments that are in the
	 * table of deposits by user
	 */
	@Override
	public List<String> showComb(String aUser) {
		List<String> lCombs = new FastList<String>();
		String lComb;
		DBObject lDocument;
		DBCursor lCursor = mCollectionPurchasing.find(
				new BasicDBObject("user", aUser));
		while (lCursor.hasNext()) {
			lDocument = lCursor.next();
			lComb = (lDocument.get("name").toString());
			lCombs.add(lComb);
		}
		return lCombs;
	}

	/**
	 *
	 * @param aUserLogin user application
	 * @param aName name of the instruments
	 * @return Integer amount of selected instruments that are in the table of
	 * deposits for user.
	 */
	@Override
	public Integer chart(String aUserLogin, String aName) {
		DBObject lDocument = mCollectionPurchasing.findOne(
				new BasicDBObject("user", aUserLogin).append("name", aName));
		Integer lcant = Integer.valueOf(lDocument.get("cant").toString());
		return lcant;
	}
}
