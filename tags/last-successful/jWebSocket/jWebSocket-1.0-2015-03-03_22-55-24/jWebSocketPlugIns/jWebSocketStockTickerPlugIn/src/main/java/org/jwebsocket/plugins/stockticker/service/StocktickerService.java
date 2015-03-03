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
package org.jwebsocket.plugins.stockticker.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import org.jwebsocket.plugins.stockticker.Implementation.Purchasing;
import org.jwebsocket.plugins.stockticker.Implementation.Record;
import org.jwebsocket.plugins.stockticker.StocktickerPlugIn;
import org.jwebsocket.plugins.stockticker.api.IPurchasing;
import org.jwebsocket.plugins.stockticker.api.IRecord;
import org.jwebsocket.plugins.stockticker.api.IService;
import org.jwebsocket.plugins.stockticker.api.IUser;
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
	private DBCollection mCollectionTicker;
	private DBCollection mCollectionPurchasing;
	private StocktickerPlugIn mPlugIn;

	/**
	 *
	 * @return
	 */
	public StocktickerPlugIn getPlugIn() {
		return mPlugIn;
	}

	/**
	 *
	 * @param aPlugIn
	 */
	public void setPlugIn(StocktickerPlugIn aPlugIn) {
		this.mPlugIn = aPlugIn;
	}

	/**
	 *
	 */
	public StocktickerService() {
		try {
			mConnection = new Mongo();
			mCollectionUser = mConnection.getDB("StockTicker").
					getCollection("Users");
			mCollectionTicker = mConnection.getDB("StockTicker").
					getCollection("Tickers");
			mCollectionPurchasing = mConnection.getDB("StockTicker").
					getCollection("Purchasing");

			createCollection();
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
	 */
	public void createCollection() {
		if (!mConnection.getDB("StockTicker").collectionExists("Tickers")) {
			List history = new FastList<Object>();
			history.add(90.1);
			history.add(91.5);
			history.add(99.3);
			history.add(94.8);
			history.add(96.1);
			history.add(97.7);
			history.add(91.6);
			history.add(98.3);
			history.add(90.7);
			history.add(98.6);

			BasicDBObject ibm = new BasicDBObject();
			ibm.put("id", 1);
			ibm.put("name", "IBM");
			ibm.put("bid", 99.5);
			ibm.put("price", 90.7);
			ibm.put("ask", 90.7);
			ibm.put("chng", 0.3);
			ibm.put("trend", 1);
			ibm.put("history", history);

			BasicDBObject google = new BasicDBObject();
			google.put("id", 2);
			google.put("name", "Google");
			google.put("bid", 98.3);
			google.put("price", 92.9);
			google.put("ask", 91.2);
			google.put("chng", 0.1);
			google.put("trend", 0);
			google.put("history", history);

			BasicDBObject microsoft = new BasicDBObject();
			microsoft.put("id", 3);
			microsoft.put("name", "Microsoft");
			microsoft.put("bid", 94.5);
			microsoft.put("price", 97.3);
			microsoft.put("ask", 92.4);
			microsoft.put("chng", 0.5);
			microsoft.put("trend", 1);
			microsoft.put("history", history);

			BasicDBObject intel = new BasicDBObject();
			intel.put("id", 4);
			intel.put("name", "Intel");
			intel.put("bid", 96.3);
			intel.put("price", 92.1);
			intel.put("ask", 95.8);
			intel.put("chng", 0.7);
			intel.put("trend", 2);
			intel.put("history", history);

			BasicDBObject oracle = new BasicDBObject();
			oracle.put("id", 5);
			oracle.put("name", "Oracle");
			oracle.put("bid", 91.8);
			oracle.put("price", 98.2);
			oracle.put("ask", 94.7);
			oracle.put("chng", 0.4);
			oracle.put("trend", 1);
			oracle.put("history", history);

			BasicDBObject apple = new BasicDBObject();
			apple.put("id", 6);
			apple.put("name", "Apple");
			apple.put("bid", 95.7);
			apple.put("price", 91.8);
			apple.put("ask", 97.9);
			apple.put("chng", 0.9);
			apple.put("trend", 0);
			apple.put("history", history);

			BasicDBObject nvidea = new BasicDBObject();
			nvidea.put("id", 7);
			nvidea.put("name", "NVIDEA");
			nvidea.put("bid", 99.6);
			nvidea.put("price", 93.2);
			nvidea.put("ask", 91.5);
			nvidea.put("chng", 0.3);
			nvidea.put("trend", 1);
			nvidea.put("history", history);

			BasicDBObject sony = new BasicDBObject();
			sony.put("id", 8);
			sony.put("name", "Sony");
			sony.put("bid", 92.6);
			sony.put("price", 93.2);
			sony.put("ask", 96.2);
			sony.put("chng", 0.8);
			sony.put("trend", 1);
			sony.put("history", history);

			BasicDBObject samsung = new BasicDBObject();
			samsung.put("id", 9);
			samsung.put("name", "Samsung");
			samsung.put("bid", 97.2);
			samsung.put("price", 96.6);
			samsung.put("ask", 91.4);
			samsung.put("chng", 0.3);
			samsung.put("trend", 0);
			samsung.put("history", history);

			BasicDBObject motorola = new BasicDBObject();
			motorola.put("id", 10);
			motorola.put("name", "Motorola");
			motorola.put("bid", 92.2);
			motorola.put("price", 93.2);
			motorola.put("ask", 91.4);
			motorola.put("chng", 0.7);
			motorola.put("trend", 1);
			motorola.put("history", history);

			mConnection.getDB("StockTicker").createCollection("Tickers", null);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(ibm);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(google);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(microsoft);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(intel);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(oracle);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(apple);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(nvidea);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(sony);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(samsung);
			mConnection.getDB("StockTicker").getCollection("Tickers").insert(motorola);
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
		return lObject != null;
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
		List<Double> lHistory = (List<Double>) mCollectionTicker.findOne(new BasicDBObject().append("id", aId)).get("history");

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
					mCollectionTicker.update(
							new BasicDBObject().append("id", ltemp),
							valuesOfRecords(ltemp, mCollectionTicker.findOne(
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
		DBCursor lCursor = mCollectionTicker.find();
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
					Double lInversionTicker = Double.valueOf((mCollectionTicker.findOne(
							new BasicDBObject("name", aName)).get("price").toString()));
					List<Double> lHistory = (List<Double>) (mCollectionTicker.findOne(
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
				Double lInversionTicker = Double.valueOf((mCollectionTicker.findOne(
						new BasicDBObject("name", aName)).get("price").toString()));
				List<Double> lHistory = (List<Double>) (mCollectionTicker.findOne(
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
		Double lInversionTicker = Double.valueOf((mCollectionTicker.findOne(
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
	 * @return list of purchasing for user
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
