//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Monitoring Plug-in
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.monitoring;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javolution.util.FastList;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;

/**
 * @author Merly, Orlando
 */
public class MonitoringPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(MonitoringPlugIn.class);
	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>();
	private static Thread mInformationThread;
	private static Thread mServerExchangeInfoThread;
	private static boolean mInformationRunning = true;
	private static int mMemory[];
	private static float mCpu;
	private static double mUsedMemPercent;
	private static double mFreeMemPercent;
	private Sigar mSigar;
	private CpuPerc[] mCPUPercent;
	private File[] mRoots;
	private NetStat mNetwork;
	private boolean mIsActive = false;
	private SimpleDateFormat mFormat;
	private DBCollection mDBColl;
	private static int mConnectedUsers = 0;

	public MonitoringPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		this.setNamespace(aConfiguration.getNamespace());
		if (mLog.isDebugEnabled()) {
			mLog.debug("Monitoring plug-in instantiated correctly.");
		}

		// Getting server exchanges
		mFormat = new SimpleDateFormat("MM/dd/yyyy");

		mDBColl = null;
		try {
			Mongo lMongo = new Mongo();
			if (null != lMongo) {
				DB lDB = lMongo.getDB("db_charting");
				if (null != lDB) {
					mDBColl = lDB.getCollection("exchanges_server");
				} else {
					mLog.error("Mongo db_charting collection could not be obtained.");
				}
			} else {
				mLog.error("Mongo DB instance could not be created.");
			}
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "initializing MongoDB connection"));
		}
		if (null == mDBColl) {
			mLog.error("MongoDB collection exchanges_server could not be obtained.");
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		mInformationThread = new Thread(new getInfo());
		mInformationThread.start();

		mServerExchangeInfoThread = new Thread(new getServerExchangeInfo());
		mServerExchangeInfoThread.start();
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		mInformationRunning = false;
		try {
			mInformationThread.join(2000);
			mInformationThread.stop();
		} catch (InterruptedException ex) {
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		mConnectedUsers++;
		if (!mIsActive) {
			getServer().getListeners().add(new ServerRequestListener());
			mIsActive = true;
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		mConnectedUsers--;
		if (mClients.contains(aConnector)) {
			mClients.remove(aConnector);
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {

			if (aToken.getType().equals("register")) {
				String lInterest = aToken.getString("interest");
				if (null != lInterest && !lInterest.isEmpty()) {
					aConnector.setVar("interest", aToken.getString("interest"));

					if ("serverXchgInfo".equals(lInterest)) {
						String lDay = aToken.getString("day");
						String lMonth = aToken.getString("month");
						String lYear = aToken.getString("year");

						Integer lCurrentYear = 0;
						if (lYear != null) {
							lCurrentYear = Integer.parseInt(lYear) + 2000;
						}

						String lCurrentDate = lMonth + "/" + lDay + "/" + lCurrentYear;
						String lToday = mFormat.format(new Date());

						if (((lDay == null) || (lYear == null) || (lMonth == null)) || (lCurrentDate.equals(lToday))) {
							aConnector.setVar("currentDate", true);
						} else {
							aConnector.setVar("currentDate", false);
							broadcastServerXchgInfoPreviousDate(aConnector, lDay, lMonth, lCurrentYear.toString());
						}
					} else if ("serverXchgInfoXDays".equals(lInterest)) {
						//obtienes el mes y el anno del cliente                        
						String lMonth = aToken.getString("month");

						// si el mes es el actual {
						aConnector.setVar("month", lMonth);
						if (lMonth != null) {
							aConnector.setVar("currentMonth", true);
						} else {
							aConnector.setVar("currentMonth", false);
							broadcastServerXchgInfoXDay(aConnector, lMonth);
						}
					} else if ("serverXchgInfoXMonth".equals(lInterest)) {
						//obtienes anno del cliente                        
						String lYear = aToken.getString("year");
						System.out.println(lYear);

						// si el anno es el actual {
						aConnector.setVar("year", lYear);
						if (lYear != null) {
							aConnector.setVar("currentYear", true);
						} else {
							aConnector.setVar("currentYear", false);
							broadcastServerXchgInfoXDay(aConnector, lYear);
						}
					}
					mClients.add(aConnector);
				}

			} else if (aToken.getType().equals("unregister")) {
				mClients.remove(aConnector);
			}
		}
	}

	class getInfo implements Runnable {

		@Override
		public void run() {
			mSigar = new Sigar();
			while (mInformationRunning) {
				gatherComputerInfo();
				Token lPCInfoToken = computerInfoToToken();

				for (WebSocketConnector lConnector : mClients) {

					String lInterest = lConnector.getString("interest");

					if ("computerInfo".equals(lInterest)) {
						getServer().sendToken(lConnector, lPCInfoToken);
					} else if ("userInfo".equals(lInterest)) {
						broadcastUserInfo(lConnector);
					} else if ("browserInfo".equals(lInterest)) {
						//TODO: Get the browsers info
//                            gatherBrowsersInfo();
						broadcastBrowsersInfo(lConnector);
					}
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	class getServerExchangeInfo implements Runnable {

		@Override
		public void run() {
			while (mInformationRunning) {
				for (WebSocketConnector lConnector : mClients) {
					if ("serverXchgInfo".equals(lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentDate") == true) {
							broadcastServerXchgInfo(lConnector);
						}
					}
					if ("serverXchgInfoXDays".equals(lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentMonth") == true) {
							broadcastServerXchgInfoXDay(lConnector, lConnector.getVar("month").toString());
						}
					}
					if ("serverXchgInfoXMonth".equals(lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentYear") == true) {
							broadcastServerXchgInfoXMonth(lConnector, lConnector.getVar("year").toString());
						}
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	public Token computerInfoToToken() {
		Token lToken = TokenFactory.createToken(getNamespace(), "computerInfo");
		//Memory Information
		lToken.setInteger("totalMem", mMemory[0]);
		lToken.setInteger("usedMem", mMemory[1]);
		lToken.setDouble("usedMemPercent", mUsedMemPercent);
		lToken.setDouble("freeMemPercent", mFreeMemPercent);
		lToken.setInteger("totalSwap", mMemory[2]);
		lToken.setInteger("usedSwap", mMemory[3]);
		lToken.setInteger("netReceived", mNetwork.getAllInboundTotal());
		lToken.setInteger("netSent", mNetwork.getAllOutboundTotal());
		lToken.setDouble("swapPercent", (double) (mMemory[3] * 100 / mMemory[2]));

		FastList<String> lList = new FastList<String>();

		for (int i = 0; i < mCPUPercent.length; i++) {
			lList.add(String.valueOf(CpuPerc.format(mCPUPercent[i].getUser())));
		}
		//CPU Information
		lToken.setList("consumeCPUCharts", lList);

		lToken.setString("consumeCPU", CpuPerc.format(mCPUPercent[0].getUser()));
		lToken.setString("consumeTotal", CpuPerc.format(mCpu));

		//HDD Information
		for (File lRoot : mRoots) {
			lToken.setString("totalHddSpace", inMeasure(lRoot.getTotalSpace()));
			lToken.setString("freeHddSpace", inMeasure(lRoot.getFreeSpace()));
			lToken.setString("usedHddSpace", inMeasure(lRoot.getTotalSpace() - lRoot.getFreeSpace()));
		}

		return lToken;
	}

	public void broadcastUserInfo(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), "userInfo");
		// user information
		lToken.setInteger("connectedUsers", mConnectedUsers);

		//to send the token
		getServer().sendToken(aConnector, lToken);
	}

	public void broadcastServerXchgInfo(WebSocketConnector aConnector) {
		Token token = TokenFactory.createToken(getNamespace(), "serverXchgInfo");

		//Getting server exchanges
		try {
			String lToday = mFormat.format(new Date());
			DBObject lRecord = mDBColl.findOne(new BasicDBObject().append("date", lToday));
			token.setMap("exchanges", lRecord.toMap());

		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, token);
	}

	public void broadcastServerXchgInfoPreviousDate(WebSocketConnector aConnector, String aDay, String aMonth, String aYear) {
		Token token = TokenFactory.createToken(getNamespace(), "serverXchgInfo");
		//Getting server exchanges
		try {
			String lToday = aMonth + "/" + aDay + "/" + aYear;

			DBObject lRecord = mDBColl.findOne(new BasicDBObject().append("date", lToday));

			token.setMap("exchanges", lRecord.toMap());


		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, token);

	}

	public void broadcastServerXchgInfoXDay(WebSocketConnector aConnector, String aMonth) {
		Token token = TokenFactory.createToken(getNamespace(), "serverXchgInfoXDays");
		//Getting server exchanges
		try {

			String lMonth = aMonth;
			//DBCursor lCursor = mDBColl.find(new BasicDBObject().append("date", "/^" + lMonth + "/"));
			DBCursor lCursor = mDBColl.find();

			String lDate = null;
			Integer lTotal = 0;
			while (lCursor.hasNext()) {
				DBObject lDocument = lCursor.next();
				lDate = (String) lDocument.get("date");
				if (lDate.startsWith(lMonth)) {

					String lDay = lDate.substring(3, 5);
					for (int i = 0; i < 24; i++) {
						if ((Integer) lDocument.get("h" + i) != null) {
							lTotal += (Integer) lDocument.get("h" + i);
						}
					}

					//System.out.println(lCursor);
					token.setInteger(lDay, lTotal);
					lTotal = 0;
				}
			}

		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, token);

	}

	public void broadcastServerXchgInfoXMonth(WebSocketConnector aConnector, String aYear) {
		Token token = TokenFactory.createToken(getNamespace(), "serverXchgInfoXMonth");
		//Getting server exchanges
		try {

			String lYear = aYear;
			//DBCursor lCursor = mDBColl.find(new BasicDBObject().append("date", "/^" + lMonth + "/"));
			DBCursor lCursor = mDBColl.find();

			String lDate = null;
			Integer lTotal = 0;
			while (lCursor.hasNext()) {
				DBObject lDocument = lCursor.next();
				lDate = (String) lDocument.get("date");
				if (lDate.endsWith(lYear)) {
					for (int lMonth = 1; lMonth < 13; lMonth++) {

						String lComparableMonth = (lMonth < 10) ? "0" + String.valueOf(lMonth) : String.valueOf(lMonth);
						String lRecordMonth = lDate.substring(0, 2);

						if (lRecordMonth.equals(lComparableMonth)) {
							for (int i = 0; i < 24; i++) {
								if ((Integer) lDocument.get("h" + i) != null) {
									lTotal += (Integer) lDocument.get("h" + i);
								}
							}
						}

						token.setInteger(lRecordMonth, token.getInteger(lRecordMonth, 0) + lTotal);
						lTotal = 0;
					}
					//System.out.println(lCursor);

				}
			}

		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, token);

	}

	private void gatherComputerInfo() {
		try {
			mMemory = gatherMemInfo();
			mCPUPercent = mSigar.getCpuPercList();
			mCpu = (float) mSigar.getCpuPerc().getUser();
			mNetwork = mSigar.getNetStat();
			mRoots = File.listRoots();

			Thread.sleep(1000);
		} catch (SigarException ex) {
		} catch (InterruptedException ex) {
		}
	}

	public void broadcastBrowsersInfo(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), "browserInfo");

		//HDD Information
		lToken.setInteger("chromium", 50);
		lToken.setInteger("ie", 10);
		lToken.setInteger("firefox", 20);
		lToken.setInteger("netscape", 20);
		lToken.setInteger("opera", 5);
		lToken.setInteger("safari", 5);
		lToken.setInteger("nativeClients", 5);

		//to send the token
		getServer().sendToken(aConnector, lToken);
	}

	//To obtain all information about the memories
	public int[] gatherMemInfo() throws SigarException {
		int lMem[] = new int[4];
		Mem lMemo = mSigar.getMem();
		Swap lExchange = mSigar.getSwap();
		long lTotalMem = inMBytes(lMemo.getTotal());
		long lUsedMem = inMBytes(lMemo.getActualUsed());
		long lTotalSwap = inMBytes(lExchange.getTotal());
		long lUsedSwap = inMBytes(lExchange.getUsed());
		mUsedMemPercent = lMemo.getUsedPercent();
		mFreeMemPercent = lMemo.getFreePercent();
		lMem[0] = (int) lTotalMem;
		lMem[1] = (int) lUsedMem;
		lMem[2] = (int) lTotalSwap;
		lMem[3] = (int) lUsedSwap;

		return lMem;
	}

	//converting the memory in megabytes
	private long inMBytes(long value) {
		return ((value / 1024) / 1024);
	}
	//for convert the hdd space

	private String inMeasure(long value) {
		if (value / 1000 < 1) {
			return String.valueOf(value) + " KB";
		} else if (value / 1000 / 1000 < 1) {
			return String.valueOf(value / 1000) + " MB";
		} else {
			return String.valueOf(value / 1000 / 1000 / 1000) + " GB";
		}
	}
}
