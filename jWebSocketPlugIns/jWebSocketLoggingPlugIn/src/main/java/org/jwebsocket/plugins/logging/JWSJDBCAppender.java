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

import java.util.Map;
import org.apache.log4j.spi.LoggingEvent;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.logging.BaseAppender;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.plugins.jdbc.JDBCPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Alexander Schulze
 */
public class JWSJDBCAppender extends BaseAppender {

	private String mJDBCPlugInID;
	private String mTableName;
	private String mDBName;
	private String mCreateTableQuery;
	private String mInsertQuery;
	private String mCreateUserQuery;
	private String mJDBCConnAlias;
	private TokenPlugIn mJDBCPlugIn;

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
					System.out.println("Error caught while creating the JDBCPlugin: " + lResponse.getString("msg"));
				}
			}
		}
	}

	@Override
	public void append(LoggingEvent aLE) {
//		Object lMsg = aLE.getMessage();
//		Map lInfo = null;
//		if (null != lMsg) {
//			lInfo = getInfoMapFromMsg((String) lMsg);
//		}
//		if (null != lInfo) {
//			lMsg = lInfo.get("message");
//			lInfo.remove("message");
//		}
		JWSJDBCPatternLayout lLayout = new JWSJDBCPatternLayout(prepareQuery(mInsertQuery));
		Token lResponse = mJDBCPlugIn.invoke(null, queryToToken(lLayout.format(aLE)));
		if (0 == lResponse.getCode()) {
			System.out.println("Successfully added log output to the database.");
		} else if (-1 == lResponse.getCode()) {
			System.out.println("Failed to insert the record in the database "
					+ "with the following message:  " + lResponse.getString("msg"));
		}
//		System.out.println("[JDBC Appender]: "
//				+ aLE.getLevel().toString() + ": "
//				+ lMsg
//				+ (lInfo != null
//						? ", info: " + lInfo
//						: "")
//		);
	}

	private Token queryToToken(String aQuery) {
		Token lCreateTableToken = TokenFactory.createToken(mJDBCPlugIn.getNamespace(),
				JDBCPlugIn.TT_EXEC_SQL);
		lCreateTableToken.setString("query", prepareQuery(aQuery));
		lCreateTableToken.setString("alias", mJDBCConnAlias);
		return lCreateTableToken;
	}

	private String prepareQuery(String aQuery) {
		String lResult = "";
		if (null != aQuery) {
			lResult = aQuery.replace("${db_table}", mTableName);
		}
		return lResult;
	}

	public String getJDBCPlugInID() {
		return mJDBCPlugInID;
	}

	public void setJDBCPlugInID(String mJDBCPlugInID) {
		this.mJDBCPlugInID = mJDBCPlugInID;
	}

	public String getCreateTableQuery() {
		return mCreateTableQuery;
	}

	public void setCreateTableQuery(String mCreateTableQuery) {
		this.mCreateTableQuery = mCreateTableQuery;
	}

	public String getTableName() {
		return mTableName;
	}

	public void setTableName(String mTableName) {
		this.mTableName = mTableName;
	}

	public String getCreateUserQuery() {
		return mCreateUserQuery;
	}

	public void setCreateUserQuery(String mCreateUserQuery) {
		this.mCreateUserQuery = mCreateUserQuery;
	}

	public String getDBName() {
		return mDBName;
	}

	public void setDBName(String mDBName) {
		this.mDBName = mDBName;
	}

	public String getJDBCConnAlias() {
		return mJDBCConnAlias;
	}

	public void setJDBCConnAlias(String mJDBCConnAlias) {
		this.mJDBCConnAlias = mJDBCConnAlias;
	}

	public String getInsertQuery() {
		return mInsertQuery;
	}

	public void setInsertQuery(String mInsertQuery) {
		this.mInsertQuery = mInsertQuery;
	}

}
