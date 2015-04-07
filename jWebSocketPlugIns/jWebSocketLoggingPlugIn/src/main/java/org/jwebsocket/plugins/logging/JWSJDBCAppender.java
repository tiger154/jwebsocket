//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Appender (Community Edition, CE)
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
package org.jwebsocket.plugins.logging;

import java.util.Calendar;
import java.util.TimerTask;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.FallbackErrorHandler;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.BaseAppender;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jdbc.JDBCPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 * @author Alexander Schulze
 * @author Victor Antonio Barzana Crespo
 */
public class JWSJDBCAppender extends BaseAppender {

	private String mJDBCPlugInID;
	private String mTableName;
	private String mCreateTableQuery;
	private String mInsertQuery;
	private String mJDBCConnAlias;
	private TokenPlugIn mJDBCPlugIn;
	// TODO: this error handler is not working properly, see how we can use 
	// a different logger here
	private final ErrorHandler mErrorHandler = new FallbackErrorHandler();

	// Configuration to cleanup the database
	private String mCleanupInterval;
	private String mCleanupQuery;
	private String mLogDuration;
	private LogsCleanupTask mCleanupTask;
	private Boolean mIsCleanupRunning;

	@Override
	public void initialize() throws Exception {
		super.initialize();
		TokenServer lServer = JWebSocketFactory.getTokenServer();
		try {
			// Loading the JDBCPlugIn from the list of loaded plugins
			mJDBCPlugIn = (TokenPlugIn) lServer.getPlugInById(mJDBCPlugInID);
		} catch (Exception lEx) {
			// TODO: handle the error properly here
		}
		if (null != mJDBCPlugIn) {
			Token lResponse = mJDBCPlugIn.invoke(null, queryToToken(mCreateTableQuery));
			if (null != lResponse) {
				System.out.println(lResponse);
				if (-1 == lResponse.getCode()) {
					mErrorHandler.error("Error caught while creating the JDBCPlugin: " + lResponse.getString("msg"));
				}
			}
		}
		mCleanupTask = new LogsCleanupTask();
		mIsCleanupRunning = true;
		Tools.getTimer().scheduleAtFixedRate(mCleanupTask, 5000, getIntervalInSeconds(mCleanupInterval));
	}

	/**
	 * Used to parse the provided cleanup interval to seconds
	 *
	 * @param aCleanupInterval
	 * @return lResponse the amount of seconds occurred in the given interval
	 */
	private long getIntervalInSeconds(String aCleanupInterval) {
		long lOneSec = 1000,
				lOneMinute = lOneSec * 60,
				lOneHour = lOneMinute * 60,
				lOneDay = lOneHour * 24;
		String[] lUnitsArray;
		lUnitsArray = new String[]{"SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", "YEAR"};
		String lReplaced;
		long lValue = 0, lResult = 0;
		String lCurrentUnit = null;
		for (String lUnit : lUnitsArray) {
			if (aCleanupInterval.contains(lUnit)) {
				lReplaced = aCleanupInterval.trim().replace(lUnit, "");
				if (lReplaced.length() > 0) {
					lValue = Integer.valueOf(lReplaced.trim());
					lCurrentUnit = lUnit;
					break;
				}
			}
		}
		if (0 != lValue && null != lCurrentUnit) {
			Calendar lCalendar = Calendar.getInstance();
			if ("SECOND".equals(lCurrentUnit)) {
				lResult = lValue * lOneSec;
			} else if ("MINUTE".equals(lCurrentUnit)) {
				lResult = lValue * lOneMinute;
			} else if ("HOUR".equals(lCurrentUnit)) {
				lResult = lValue * lOneHour;
			} else if ("DAY".equals(lCurrentUnit)) {
				lResult = lValue * lOneDay;
			} else if ("WEEK".equals(lCurrentUnit)) {
				lResult = lValue * lOneDay * lCalendar.getActualMaximum(Calendar.DAY_OF_WEEK);
			} else if ("MONTH".equals(lCurrentUnit)) {
				lResult = lValue * lOneDay * lCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			} else if ("YEAR".equals(lCurrentUnit)) {
				lResult = lValue * lOneDay * lCalendar.getActualMaximum(Calendar.DAY_OF_YEAR);
			} else if ("QUARTER".equals(lCurrentUnit)) {
				lResult = lValue * lOneDay * lCalendar.getActualMaximum(Calendar.DAY_OF_YEAR) / 2;
			}
		}
		return lResult;
	}

	private class LogsCleanupTask extends TimerTask {

		@Override
		public void run() {
			if (mIsCleanupRunning && null != mJDBCPlugIn && null != mCleanupQuery) {
				mJDBCPlugIn.invoke(null, queryToToken("SET SQL_SAFE_UPDATES=0;"));
				Token lResponse = mJDBCPlugIn.invoke(null, queryToToken(mCleanupQuery));
				mJDBCPlugIn.invoke(null, queryToToken("SET SQL_SAFE_UPDATES=1;"));
				if (0 == lResponse.getCode()) {
				} else if (-1 == lResponse.getCode()) {
					mErrorHandler.error("An error was caught while running the "
							+ "cleanup mechanism: " + lResponse.getString("msg"));
				}
				System.out.println("Running Cleanup Mechanism");
			}
		}

	}

	@Override
	public void shutdown() throws Exception {
		super.shutdown();
		mIsCleanupRunning = false;
		mCleanupTask.cancel();
	}

	@Override
	public void append(LoggingEvent aLE) {
		if (null != mJDBCPlugIn && null != mInsertQuery) {
			JWSJDBCPatternLayout lLayout = new JWSJDBCPatternLayout(prepareQuery(mInsertQuery));
			Token lResponse = mJDBCPlugIn.invoke(null, queryToToken(lLayout.format(aLE)));
			if (0 == lResponse.getCode()) {
			} else if (-1 == lResponse.getCode()) {
				mErrorHandler.error("Failed to insert the record in the database "
						+ "with the following message:  " + lResponse.getString("msg"));
			}
		}
	}

	private Token queryToToken(String aQuery) {
		Token lQueryToken = TokenFactory.createToken(mJDBCPlugIn.getNamespace(),
				JDBCPlugIn.TT_EXEC_SQL_NO_LOGS);
		lQueryToken.setString("sql", prepareQuery(aQuery));
		lQueryToken.setString("alias", mJDBCConnAlias);
		return lQueryToken;
	}

	private String prepareQuery(String aQuery) {
		String lResult = "";
		if (null != aQuery) {
			lResult = aQuery.replace("${db_table}", mTableName)
					.replace("${log_duration}", mLogDuration);
		}
		return lResult;
	}

	public void setJDBCPlugInID(String aJDBCPlugInID) {
		mJDBCPlugInID = aJDBCPlugInID;
	}

	public void setCreateTableQuery(String aCreateTableQuery) {
		mCreateTableQuery = aCreateTableQuery;
	}

	public void setTableName(String aTableName) {
		mTableName = aTableName;
	}

	public void setJDBCConnAlias(String aJDBCConnAlias) {
		mJDBCConnAlias = aJDBCConnAlias;
	}

	public void setInsertQuery(String aInsertQuery) {
		mInsertQuery = aInsertQuery;
	}

	public void setCleanupInterval(String aCleanupInterval) {
		mCleanupInterval = aCleanupInterval;
	}

	public void setCleanupQuery(String aCleanupQuery) {
		mCleanupQuery = aCleanupQuery;
	}

	public void setLogDuration(String aLogDuration) {
		mLogDuration = aLogDuration;
	}
}
