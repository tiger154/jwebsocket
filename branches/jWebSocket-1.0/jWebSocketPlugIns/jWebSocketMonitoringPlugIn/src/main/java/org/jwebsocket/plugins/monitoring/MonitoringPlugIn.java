//	---------------------------------------------------------------------------
//	jWebSocket Monitoring Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.monitoring;

import com.mongodb.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.hyperic.sigar.*;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * @author Merly, Orlando, vbarzana
 */
public class MonitoringPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	public final static String NS_MONITORING =
			JWebSocketServerConstants.NS_BASE + ".plugins.monitoring";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket FileSystemPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket FileSystemPlugIn - Community Edition";
	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>();
	private static Thread mInformationThread;
	private static Thread mServerExchangeInfoThread;
	private static Thread mUserInfoThread;
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
	private boolean mUserInfoRunning = false;
	private SimpleDateFormat mFormat;
	private DBCollection mDBColl;
	private DBCollection mUsePlugInsColl;
	private static int mConnectedUsers = 0;
	private static int mTimeCounter = 0;
	private static FastList<Integer> mConnectedUsersList = new FastList<Integer>(60);
	private final static String TT_REGISTER = "register";
	private final static String TT_UNREGISTER = "unregister";
	private final static String TT_SERVER_XCHG_INFO = "serverXchgInfo";
	private final static String TT_SERVER_XCHG_INFO_DAYS = "serverXchgInfoXDays";
	private final static String TT_SERVER_XCHG_INFO_MONTH = "serverXchgInfoXMonth";
	private final static String TT_USER_INFO = "userInfo";
	private final static String TT_PC_INFO = "computerInfo";
	private final static String TT_PLUGINS_INFO = "pluginsInfo";

	/**
	 *
	 * @param aConfiguration
	 */
	public MonitoringPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Monitoring plug-in...");
		}
		// specify default name space for monitoring plugin
		this.setNamespace(NS_MONITORING);

		// Getting server exchanges
		mFormat = new SimpleDateFormat("MM/dd/yyyy");

		mDBColl = null;
		try {
			Mongo lMongo = new Mongo();
			if (null != lMongo) {
				DB lDB = lMongo.getDB("db_charting");
				if (null != lDB) {
					mDBColl = lDB.getCollection("exchanges_server");
					mUsePlugInsColl = lDB.getCollection("use_plugins");
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
		} else if (mLog.isInfoEnabled()) {
			mLog.info("Monitoring Plug-in successfully instantiated.");
		}
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getVendor() {
		return VENDOR;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT;
	}

	@Override
	public String getLicense() {
		return LICENSE;
	}

	@Override
	public String getNamespace() {
		return NS_MONITORING;
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		//Initializing thread
		mInformationRunning = true;
		mInformationThread = new Thread(new getInfo(), "jWebSocket Monitoring Plug-in Information");
		mInformationThread.start();

		mServerExchangeInfoThread = new Thread(new getServerExchangeInfo(), "jWebSocket Monitoring Plug-in Server Exchange");
		mServerExchangeInfoThread.start();

		mUserInfoThread = new Thread(new getUserInfo(), "jWebSocket Monitoring Plug-in UserInformation");
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		mInformationRunning = false;

		try {
			mInformationThread.join(2000);
			mInformationThread.stop();
		} catch (InterruptedException ex) {
		}

		try {
			mServerExchangeInfoThread.join(2000);
			mServerExchangeInfoThread.stop();
		} catch (InterruptedException ex) {
		}

		try {
			mUserInfoThread.join(2000);
			mUserInfoThread.stop();
		} catch (InterruptedException ex) {
		}

	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		mConnectedUsers++;
		if (!mUserInfoRunning) {
			mUserInfoRunning = true;
			mUserInfoThread.start();
		}
		if (!mIsActive) {
			try {
				getServer().getListeners().add(new ServerRequestListener());
				mIsActive = true;
			} catch (Exception lEx) {
				System.out.println(lEx.getMessage());
			}
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		mConnectedUsers--;
		if (mClients.contains(aConnector)) {
			mClients.remove(aConnector);
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {

			if (aToken.getType().equals(TT_REGISTER)) {
				String lInterest = aToken.getString("interest");
				if (null != lInterest && !lInterest.isEmpty()) {
					aConnector.setVar("interest", aToken.getString("interest"));

					if (TT_SERVER_XCHG_INFO.equals(lInterest)) {
						String lDay = aToken.getString("day");
						String lMonth = aToken.getString("month");
						String lYear = aToken.getString("year");

						Integer lCurrentYear = 0;
						if (lYear != null) {
							lCurrentYear = Integer.parseInt(lYear) + 2000;
						}

						String lCurrentDate = lMonth + "/" + lDay + "/"
								+ lCurrentYear;
						String lToday = mFormat.format(new Date());

						if (((lDay == null)
								|| (lYear == null)
								|| (lMonth == null))
								|| (lCurrentDate.equals(lToday))) {
							aConnector.setVar("currentDate", true);
						} else {
							aConnector.setVar("currentDate", false);
							broadcastServerXchgInfoPreviousDate(aConnector,
									lDay, lMonth, lCurrentYear.toString());
						}
					} else if (TT_SERVER_XCHG_INFO_DAYS.equals(lInterest)) {
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
					} else if (TT_SERVER_XCHG_INFO_MONTH.equals(lInterest)) {
						//obtienes anno del cliente                        
						String lYear = aToken.getString("year");
						//System.out.println(lYear);

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

			} else if (aToken.getType().equals(TT_UNREGISTER)) {
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

					if (TT_PC_INFO.equals(lInterest)) {

						getServer().sendToken(lConnector, lPCInfoToken);

					} else if (TT_PLUGINS_INFO.equals(lInterest)) {
						//TODO: Get the plugins info
//                            gatherBrowsersInfo();
						broadcastPluginsInfo(lConnector);
					}
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

	class getUserInfo implements Runnable {

		@Override
		public void run() {

			while (mUserInfoRunning) {
				mConnectedUsersList.add(mTimeCounter, mConnectedUsers);
				Token lToken = TokenFactory.createToken(getNamespace(),
						TT_USER_INFO);
				lToken.setList("connectedUsers", mConnectedUsersList);

				String lInterest = "";
				for (WebSocketConnector lConnector : mClients) {

					lInterest = lConnector.getString("interest");

					if (TT_USER_INFO.equals(lInterest)) {
						getServer().sendToken(lConnector, lToken);
					}
				}
				try {
					Thread.sleep(1000);
					if (mTimeCounter == 59) {
						mConnectedUsersList.removeFirst();
						mTimeCounter--;
					}
				} catch (InterruptedException ex) {
				}
				mTimeCounter++;
			}
		}
	}

	class getServerExchangeInfo implements Runnable {

		@Override
		public void run() {
			while (mInformationRunning) {
				for (WebSocketConnector lConnector : mClients) {
					if (TT_SERVER_XCHG_INFO.equals(
							lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentDate") == true) {
							broadcastServerXchgInfo(lConnector);
						}
					}
					if (TT_SERVER_XCHG_INFO_DAYS.equals(
							lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentMonth") == true) {
							broadcastServerXchgInfoXDay(lConnector,
									lConnector.getVar("month").toString());
						}
					}
					if (TT_SERVER_XCHG_INFO_MONTH.equals(
							lConnector.getString("interest"))) {
						if (lConnector.getBoolean("currentYear") == true) {
							broadcastServerXchgInfoXMonth(lConnector,
									lConnector.getVar("year").toString());
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

	/**
	 *
	 * @return
	 */
	public Token computerInfoToToken() {
		Token lToken = TokenFactory.createToken(getNamespace(), TT_PC_INFO);
		//Memory Information
		lToken.setInteger("totalMem", mMemory[0]);
		lToken.setInteger("usedMem", mMemory[1]);
		lToken.setDouble("usedMemPercent", mUsedMemPercent);
		lToken.setDouble("freeMemPercent", mFreeMemPercent);
		lToken.setInteger("totalSwap", mMemory[2]);
		lToken.setInteger("usedSwap", mMemory[3]);
		lToken.setInteger("netReceived", mNetwork.getAllInboundTotal());
		lToken.setInteger("netSent", mNetwork.getAllOutboundTotal());
		lToken.setDouble("swapPercent", (double) (mMemory[3] * 100
				/ mMemory[2]));

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
			lToken.setString("usedHddSpace", inMeasure(lRoot.getTotalSpace()
					- lRoot.getFreeSpace()));
		}

		return lToken;
	}

	/**
	 *
	 * @param aConnector
	 */
	public void broadcastServerXchgInfo(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(),
				TT_SERVER_XCHG_INFO);

		//Getting server exchanges
		try {
			String lToday = mFormat.format(new Date());
			DBObject lRecord = mDBColl.findOne(
					new BasicDBObject().append("date", lToday));
			lToken.setMap("exchanges", lRecord.toMap());

		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, lToken);
	}

	/**
	 *
	 * @param aConnector
	 * @param aDay
	 * @param aMonth
	 * @param aYear
	 */
	public void broadcastServerXchgInfoPreviousDate(WebSocketConnector aConnector, String aDay, String aMonth, String aYear) {
		Token token = TokenFactory.createToken(getNamespace(),
				TT_SERVER_XCHG_INFO);
		//Getting server exchanges
		try {
			String lToday = aMonth + "/" + aDay + "/" + aYear;

			DBObject lRecord = mDBColl.findOne(
					new BasicDBObject().append("date", lToday));

			token.setMap("exchanges", lRecord.toMap());


		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, token);

	}

	/**
	 *
	 * @param aConnector
	 * @param aMonth
	 */
	public void broadcastServerXchgInfoXDay(WebSocketConnector aConnector,
			String aMonth) {
		Token token = TokenFactory.createToken(getNamespace(), TT_SERVER_XCHG_INFO_DAYS);
		//Getting server exchanges
		try {
			String lMonth = aMonth;
			//DBCursor lCursor = mDBColl.find(
			//new BasicDBObject().append("date", "/^" + lMonth + "/"));
			DBCursor lCursor = mDBColl.find();

			boolean m = false;
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
					m = true;
				}
			}
			if (m == false) {
				token.setInteger("code", -1);
				//token.setString("msg", ex.getMessage());
				//throw new Exception("Error");
			}

		} catch (Exception ex) {
			mLog.error(ex.getMessage());
//			token.setInteger("code", -1);
//			token.setString("msg", ex.getMessage());
		}

		getServer().sendToken(aConnector, token);

	}

	/**
	 *
	 * @param aConnector
	 * @param aYear
	 */
	public void broadcastServerXchgInfoXMonth(WebSocketConnector aConnector,
			String aYear) {
		Token token = TokenFactory.createToken(getNamespace(),
				TT_SERVER_XCHG_INFO_MONTH);
		//Getting server exchanges
		try {
			String lYear = aYear;
			//DBCursor lCursor = mDBColl.find(
			//new BasicDBObject().append("date", "/^" + lMonth + "/"));
			DBCursor lCursor = mDBColl.find();

			boolean m = false;
			String lDate = null;
			Integer lTotal = 0;
			while (lCursor.hasNext()) {
				DBObject lDocument = lCursor.next();
				lDate = (String) lDocument.get("date");
				if (lDate.endsWith(lYear)) {
					for (int lMonth = 1; lMonth < 13; lMonth++) {

						String lComparableMonth = (lMonth < 10)
								? "0" + String.valueOf(lMonth)
								: String.valueOf(lMonth);
						String lRecordMonth = lDate.substring(0, 2);

						if (lRecordMonth.equals(lComparableMonth)) {
							for (int i = 0; i < 24; i++) {
								if ((Integer) lDocument.get("h" + i) != null) {
									lTotal += (Integer) lDocument.get("h" + i);
								}
							}
						}
						token.setInteger(lRecordMonth, token.getInteger(
								lRecordMonth, 0) + lTotal);
						lTotal = 0;
						m = true;
					}
				}
			}
			if (m == false) {
				token.setInteger("code", -1);
				//token.setString("msg", ex.getMessage());
				//throw new Exception("Error");
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

	/**
	 *
	 * @param aConnector
	 */
	public void broadcastPluginsInfo(WebSocketConnector aConnector) {
		Token lToken = TokenFactory.createToken(getNamespace(), TT_PLUGINS_INFO);
		String lNamespace = lToken.getNS();
		FastList<Map> lList = new FastList<Map>();

		try {
			DBCursor lCursor = mUsePlugInsColl.find();
			DBObject lDocument;
			FastMap<String, Object> lMap;
			while (lCursor.hasNext()) {
				lDocument = lCursor.next();
				lMap = new FastMap<String, Object>();
				lMap.put("id", lDocument.get("id"));
				lMap.put("requests", lDocument.get("requests"));

				lList.add(lMap);
			}
			lToken.setList("usePlugins", lList);
		} catch (Exception ex) {
			mLog.error(ex.getMessage());
		}

		getServer().sendToken(aConnector, lToken);
	}

	//To obtain all information about the memories
	/**
	 *
	 * @return @throws SigarException
	 * @throws SigarException
	 */
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
