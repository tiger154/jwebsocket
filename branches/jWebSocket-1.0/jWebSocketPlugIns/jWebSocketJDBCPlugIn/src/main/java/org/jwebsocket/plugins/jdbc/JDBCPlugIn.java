//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JDBC Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
//	THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
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
package org.jwebsocket.plugins.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IBasicStorage;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.spring.ServerXmlBeanFactory;
import org.jwebsocket.storage.ehcache.EhCacheStorage;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.core.io.FileSystemResource;

/**
 * 
 * @author aschulze EhChache
 */
public class JDBCPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(JDBCPlugIn.class);
	// if namespace changed updateSQL client plug-in accordingly!
	private static final String NS_JDBC = JWebSocketServerConstants.NS_BASE + ".plugins.jdbc";
	private IBasicStorage mCache = null;
	private int mConnValTimeout = 300;
	private static ServerXmlBeanFactory mBeanFactory;
	private static NativeAccess mNativeAccess;
	private static String mSelectSequenceSQL = null;
	private static String mExecFunctionSQL = null;
	private static String mExecStoredProcSQL = null;

	// TODO: Check all methods: If mNativeAccess is not set return error!
	/**
	 *
	 * @param aConfiguration
	 */
	public JDBCPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JDBC plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_JDBC);
		mCache = new EhCacheStorage(NS_JDBC);

		String lVal = getString("conn_val_timeout");

		try {
			String lSpringConfig = getString("spring_config");
			lSpringConfig = Tools.expandEnvVars(lSpringConfig);
			String lPath = FilenameUtils.getPath(lSpringConfig);
			if (lPath == null || lPath.length() <= 0) {
				lPath = JWebSocketConfig.getConfigFolder(lSpringConfig);
			} else {
				lPath = lSpringConfig;
			}
			FileSystemResource lFSRes = new FileSystemResource(lPath);

			mBeanFactory = new ServerXmlBeanFactory(lFSRes, getClass().getClassLoader());
			mNativeAccess = (NativeAccess) mBeanFactory.getBean("nativeAccess");
			if (null != mNativeAccess) {
				mSelectSequenceSQL = mNativeAccess.getSelectSequenceSQL();
				mExecFunctionSQL = mNativeAccess.getExecFunctionSQL();
				mExecStoredProcSQL = mNativeAccess.getExecStoredProcSQL();
				// give a success message to the administrator
				if (mLog.isInfoEnabled()) {
					mLog.info("JDBC plug-in successfully loaded.");
				}
			} else {
				mLog.error("Database bean could not be loaded properly.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " at JDBC plug-in instantiation: " + lEx.getMessage());
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("querySQL")) {
				// run single native query sql command (select)
				// with returning a result set
				querySQL(aConnector, aToken);
			} else if (lType.equals("updateSQL")) {
				// run single native updateSQL sql command (updateSQL, delete, insert)
				// w/o returning a result set, but number of affected rows
				updateSQL(aConnector, aToken);
			} else if (lType.equals("execSQL")) {
				// run single native updateSQL sql command (updateSQL, delete, insert)
				// w/o returning a result set, but number of affected rows
				execSQL(aConnector, aToken);
			} else if (lType.equals("getNextSeqVal")) {
				// get the next value of a sequence
				getNextSeqVal(aConnector, aToken);
			} else if (lType.equals("select")) {
				// abstract select from database
				select(aConnector, aToken);
				// abstract updateSQL from database
			} else if (lType.equals("update")) {
				update(aConnector, aToken);
				// abstract delete from database
			} else if (lType.equals("delete")) {
				delete(aConnector, aToken);
				// abstract insert into database
			} else if (lType.equals("insert")) {
				insert(aConnector, aToken);
				// abstract start transaction
			} else if (lType.equals("startTA")) {
				startTA(aConnector, aToken);
				// abstract commit transaction
			} else if (lType.equals("commit")) {
				commit(aConnector, aToken);
				// abstract rollback transaction
			} else if (lType.equals("rollback")) {
				rollback(aConnector, aToken);
			} else if (lType.equals("getSecure")) {
				getSecure(aConnector, aToken);
				// run multiple abstract updateSQL sql commands (updateSQL, delete, insert)
			} else if (lType.equals("postSecure")) {
				postSecure(aConnector, aToken);
				// run multiple native query sql commands (select)
			} else if (lType.equals("getSQL")) {
				getSQL(aConnector, aToken);
				// run multiple native updateSQL sql commands (updateSQL, delete, insert)
			} else if (lType.equals("postSQL")) {
				postSQL(aConnector, aToken);
			}
		}
	}

	public DataSource getNativeDataSource() {
		return mNativeAccess.getDataSource();
	}

	public Class getJDBCTools() {
		return JDBCTools.class;
	}

	@Override
	public Token invoke(WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (lType.equals("getNextSeqVal")) {
				return getNextSeqVal(aToken);
			} else if (lType.equals("updateSQL")) {
				return updateSQL(aToken);
			} else if (lType.equals("execSQL")) {
				return execSQL(aToken);
			} else if (lType.equals("querySQL")) {
				return query(aToken);
			}
		}
		return null;
	}

	private void querySQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'querySQL'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".querySQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// run query
		Token lResToken = query(aToken);
		// send response to requester
		lServer.sendToken(aConnector, lResToken);
	}

	private Token mCheckDataSource(Token aToken) {
		TokenServer lServer = getServer();
		Token lResToken;
		if (null == mNativeAccess) {
			lResToken = lServer.createErrorToken(aToken,
					-1, "No database connection available.");
		} else {
			lResToken = lServer.createResponse(aToken);
		}
		return lResToken;
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private Token query(Token aToken) {
		Token lResToken = mCheckDataSource(aToken);
		if (0 != lResToken.getInteger("code")) {
			return lResToken;
		}
		// load SQL query string
		String lSQL = aToken.getString("sql");
		// load SQL script
		List<String> lScript = aToken.getList("script");
		// load expiration, default is no cache (expiration = 0)
		Integer lExpiration = aToken.getInteger("expiration", 0);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'query'...");
		}

		Token lSQLResponse;
		List<String> lDetails = new FastList<String>();
		List<Map> lResultSets = new FastList<Map>();
		lResToken.setList("details", lDetails);
		lResToken.setList("resultSets", lResultSets);

		if (lScript != null) {
			for (String lSQLn : lScript) {
				lSQLResponse = mNativeAccess.query(lSQLn);
				Map<String, Object> lResultSet = new FastMap<String, Object>();
				lResultSet.put("colcount", lSQLResponse.getInteger("colcount", -1));
				lResultSet.put("rowcount", lSQLResponse.getInteger("rowcount", -1));
				lResultSet.put("columns", lSQLResponse.getList("columns"));
				lResultSet.put("data", lSQLResponse.getList("data"));
				lResultSets.add(lResultSet);
			}
		}
		if (lSQL != null) {
			lSQLResponse = mNativeAccess.query(lSQL);
			Integer lCode = lSQLResponse.getInteger("code");
			if (0 == lCode) {
				lResToken.setInteger("colcount", lSQLResponse.getInteger("colcount", -1));
				lResToken.setInteger("rowcount", lSQLResponse.getInteger("rowcount", -1));
				lResToken.setList("columns", lSQLResponse.getList("columns"));
				lResToken.setList("data", lSQLResponse.getList("data"));
			} else {
				lResToken.setInteger("code", lCode);
				lResToken.setString("msg", lSQLResponse.getString("msg"));
			}
		}
		// send response to requester
		return lResToken;
	}

	private Token getNextSeqVal(Token aToken) {
		Token lResToken = mCheckDataSource(aToken);
		if (0 != lResToken.getInteger("code")) {
			return lResToken;
		}

		String lSequence = aToken.getString("sequence");
		Integer lCount = aToken.getInteger("count", 1);
		if (lSequence != null) {
			Map<String, String> lVars = new FastMap<String, String>();
			lVars.put("sequence", lSequence);
			List<Integer> lValues = new FastList<Integer>();
			String lErrMsg = null;
			for (int lValIdx = 0; lValIdx < lCount; lValIdx++) {
				String lQuery = Tools.expandVars(mSelectSequenceSQL, lVars, Tools.EXPAND_CASE_SENSITIVE);
				Token lPKToken = mNativeAccess.query(lQuery);
				if (0 == lPKToken.getInteger("code")) {
					Number lNextSeqVal = null;
					List lRows = lPKToken.getList("data");
					if (lRows != null) {
						List lFields = (List) lRows.get(0);
						if (lFields != null) {
							lNextSeqVal = (Number) lFields.get(0);
							lValues.add(lNextSeqVal.intValue());
						}
					}
				} else {
					lErrMsg = lPKToken.getString("msg");
					break;
				}
			}
			if (null == lErrMsg && lValues.size() > 0) {
				lResToken.setInteger("code", 0);
				lResToken.setList("values", lValues);
			} else {
				lResToken.setInteger("code", -1);
				lResToken.setString("msg", "value could not be obtained: " + lErrMsg);
			}
		} else {
			lResToken.setInteger("code", -1);
			lResToken.setString("msg", "no sequence given");
		}
		return lResToken;
	}

	private void getNextSeqVal(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		// send response to requester
		lServer.sendToken(aConnector, getNextSeqVal(aToken));
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private Token updateSQL(Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'updateSQL'...");
		}
		Token lResToken = mCheckDataSource(aToken);
		if (0 != lResToken.getInteger("code")) {
			return lResToken;
		}
		TokenServer lServer = getServer();
		// load SQL string
		String lSQL = aToken.getString("sql");
		List<String> lScript = aToken.getList("script");

		Token lSQLResult = null;
		List lDetails = new FastList<String>();
		List lRowsAffected = new FastList<Integer>();
		lResToken.setList("details", lDetails);
		lResToken.setList("rowsAffected", lRowsAffected);

		// first execute SQL script if such passed
		if (lScript != null) {
			for (String lSQLn : lScript) {
				lSQLResult = mNativeAccess.update(lSQLn);
				if (lSQLResult.getInteger("code", 0) != 0) {
					lResToken.setInteger("code", -1);
					lResToken.setString("msg", "Update error. Please refer to 'details' field.");
					lDetails.add(lSQLn + ": " + lSQLResult + "\n");
				}
				lRowsAffected.add(lSQLResult.getInteger("rowsAffected", -1));
			}
		}
		// then execute single SQL if such passed
		if (lSQL != null) {
			lSQLResult = mNativeAccess.update(lSQL);
			if (lSQLResult.getInteger("code", 0) != 0) {
				lResToken.setInteger("code", -1);
				lResToken.setString("msg", "Update error. Please refer to 'details' field.");
				lDetails.add(lSQL + ": " + lSQLResult + "\n");
			}
			lRowsAffected.add(lSQLResult.getInteger("rowsAffected", -1));
		}

		return lResToken;
	}

	private void updateSQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// check if user is allowed to run 'updateSQL' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".updateSQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		Token lResToken = updateSQL(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResToken);
	}

	private Token execSQL(Token aToken) {
		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'execSQL'...");
		}
		Token lResToken = mCheckDataSource(aToken);
		if (0 != lResToken.getInteger("code")) {
			return lResToken;
		}

		TokenServer lServer = getServer();
		// load SQL string
		String lSQL = aToken.getString("sql");
		Token lExecToken = mNativeAccess.exec(lSQL);
		lServer.setResponseFields(aToken, lExecToken);
		return lExecToken;
	}

	private void execSQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".execSQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// send response to requester
		lServer.sendToken(aConnector, execSQL(aToken));
	}

	// field names may be of type string only!
	private String validateFieldsString(List<String> aFields) {
		if (aFields == null) {
			return null;
		}
		StringBuilder lRes = new StringBuilder();
		int lCnt = aFields.size();
		int lIdx = 0;
		for (String lField : aFields) {
			lRes.append(lField);
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	// values may be of variable types!
	private String validateValuesString(List aValues) {
		if (aValues == null) {
			return null;
		}
		StringBuilder lRes = new StringBuilder();
		int lCnt = aValues.size();
		int lIdx = 0;
		for (Object lValue : aValues) {
			lRes.append(JDBCTools.valueToString(lValue));
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	// table names may be of type string only!
	private String validateTablesString(List<String> aTables) {
		if (aTables == null) {
			return null;
		}
		StringBuilder lRes = new StringBuilder();
		int lCnt = aTables.size();
		int lIdx = 0;
		for (String lTable : aTables) {
			lRes.append(lTable);
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	private String validateOrdersString(List<String> aOrders) {
		if (aOrders == null) {
			return null;
		}
		StringBuilder lRes = new StringBuilder();
		int lCnt = aOrders.size();
		int lIdx = 0;
		for (String lOrder : aOrders) {
			lRes.append(lOrder);
			lIdx++;
			if (lIdx < lCnt) {
				lRes.append(",");
			}
		}
		return lRes.toString();
	}

	/**
	 *
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void select(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'select'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".select")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		// obtain required parameters for query
		List lTables = aToken.getList("tables");
		List lFields = aToken.getList("fields");
		List lOrders = aToken.getList("orders");

		if (lTables == null || lTables.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No tables passed for JDBC select."));
			return;
		}
		if (lFields == null || lFields.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No fields passed for JDBC select."));
			return;
		}

		String lTablesStr = validateTablesString(lTables);
		String lFieldsStr = validateFieldsString(lFields);
		String lOrdersStr = validateOrdersString(lOrders);

		String lWhere = aToken.getString("where");
		String lGroup = aToken.getString("group");
		String lHaving = aToken.getString("having");

		// load expiration, default is no cache (expiration = 0)
		Integer lExpiration = aToken.getInteger("expiration", 0);

		// build SQL string
		String lSQL =
				"select "
				+ lFieldsStr
				+ " from "
				+ lTablesStr;

		// add where condition
		if (lWhere != null && lWhere.length() > 0) {
			lSQL += " where " + lWhere;
		}
		// add order options
		if (lOrdersStr != null && lOrdersStr.length() > 0) {
			lSQL += " order by " + lOrdersStr;
		}

		Token lQueryToken = TokenFactory.createToken();
		lQueryToken.setString("sql", lSQL);
		Token lResponse = query(lQueryToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void update(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'update'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".update")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lTable = aToken.getString("table");
		List lFields = aToken.getList("fields");
		List lValues = aToken.getList("values");
		String lWhere = aToken.getString("where");

		if (lTable == null || lTable.length() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No table passed for JDBC update."));
			return;
		}
		if (lFields == null || lFields.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No fields passed for JDBC update."));
			return;
		}
		if (lValues == null || lValues.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No values passed for JDBC update."));
			return;
		}
		if (lFields.size() != lValues.size()) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"Number of values doe not match number of fields in JDBC update."));
			return;
		}

		StringBuilder lSetStr = new StringBuilder();

		int lIdx = 0;
		int lCnt = lFields.size();
		for (Object lField : lFields) {
			lSetStr.append((String) lField);
			lSetStr.append("=");
			lSetStr.append(JDBCTools.valueToString(lValues.get(lIdx)));
			lIdx++;
			if (lIdx < lCnt) {
				lSetStr.append(",");
			}
		}

		String lSQL = "update"
				+ " " + lTable
				+ " set"
				+ " " + lSetStr.toString();
		if (lWhere != null) {
			lSQL += " where"
					+ " " + lWhere;
		}

		Token lUpdateToken = TokenFactory.createToken();
		lUpdateToken.setString("sql", lSQL);
		Token lResponse = updateSQL(lUpdateToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void insert(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'insert'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".insert")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lTable = aToken.getString("table");
		List lFields = aToken.getList("fields");
		List lValues = aToken.getList("values");

		String lFieldsStr = validateFieldsString(lFields);
		String lValuesStr = validateValuesString(lValues);

		if (lTable == null || lTable.length() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No table passed for JDBC insert."));
			return;
		}
		if (lFields == null || lFields.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No fields passed for JDBC insert."));
			return;
		}
		if (lValues == null || lValues.size() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No values passed for JDBC insert."));
			return;
		}
		if (lFields.size() != lValues.size()) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"Number of values doe not match number of fields in JDBC insert."));
			return;
		}

		String lSQL = "insert into"
				+ " " + lTable
				+ " (" + lFieldsStr + ")"
				+ " values"
				+ " (" + lValuesStr + ")";

		Token lInsertToken = TokenFactory.createToken();
		lInsertToken.setString("sql", lSQL);
		Token lResponse = updateSQL(lInsertToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void delete(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'delete'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".delete")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			return;
		}

		String lTable = aToken.getString("table");
		String lWhere = aToken.getString("where");

		if (lTable == null || lTable.length() <= 0) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(aToken, -1,
					"No table passed for JDBC delete."));
			return;
		}

		String lSQL = "delete from"
				+ " " + lTable;
		if (lWhere != null) {
			lSQL += " where"
					+ " " + lWhere;
		}

		Token lInsertToken = TokenFactory.createToken();
		lInsertToken.setString("sql", lSQL);
		Token lResponse = updateSQL(lInsertToken);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private Token mQuerySQL(String aSQL, int aExpiration) {


		String lHash = "hash";
		Token lResponse = null;

		// check cache before running the query.
		if (aExpiration > 0) {
			lHash = Tools.getMD5(aSQL);
			lResponse = (Token) mCache.get(lHash);
			if (lResponse != null) {
				lResponse.setBoolean("isCached", true);
				return lResponse;
			}
		}


		// if to be cached, put it to cache
		// don't save isCached flag
		if (aExpiration > 0) {
			mCache.put(lHash, lResponse);
		}

		lResponse.setBoolean("isCached", false);

		return lResponse;
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void startTA(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'startTA'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".transactions")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void rollback(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'rollback'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".transactions")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void commit(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'commit'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".transactions")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getSecure(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSecure'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".getSecure")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void postSecure(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'postSecure'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".postSecure")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void getSQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'getSQL'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".getSQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void postSQL(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		if (mLog.isDebugEnabled()) {
			mLog.debug("Processing 'postSQL'...");
		}

		// check if user is allowed to run 'select' command
		if (!SecurityFactory.hasRight(lServer.getUsername(aConnector), NS_JDBC + ".postSQL")) {
			lServer.sendToken(aConnector, lServer.createAccessDenied(aToken));
			// return;
		}

		/*
		String lSQL = aToken.getString("sql");
		Token lResponse = mExecSQL(lSQL);
		
		// send response to requester
		lServer.sendToken(aConnector, lResponse);
		 */
	}

	private void getDBInfo(WebSocketConnector aConnector, Token aToken) {

		Token lResponse = createResponse(aToken);

		List lResDrivers = new FastList();
		Enumeration lDrivers = DriverManager.getDrivers();
		Driver lDriver;
		while (lDrivers.hasMoreElements()) {
			lDriver = (Driver) lDrivers.nextElement();
			Map lDriverInfo = new FastMap();
			lDriverInfo.put("majorVersion", lDriver.getMajorVersion());
			lDriverInfo.put("minorVersion", lDriver.getMinorVersion());
			lDriverInfo.put("simpleName", lDriver.getClass().getSimpleName());
			lDriverInfo.put("className", lDriver.getClass().getName());
			lDriverInfo.put("isJdbcCompliant", lDriver.jdbcCompliant());
			lResDrivers.add(lDriverInfo);
		}
		lResponse.setList("drivers", lResDrivers);

		Connection lConn = null;
		try {
			DataSource lDataSource = getNativeDataSource();
			Map lConnInfo = new FastMap();

			lConn = lDataSource.getConnection();
			DatabaseMetaData meta = lConn.getMetaData();
			lConnInfo.put("serverName", meta.getDatabaseProductName());
			lConnInfo.put("serverVersion", meta.getDatabaseProductVersion());
			lConnInfo.put("driverName", meta.getDriverName());
			lConnInfo.put("driverVersion", meta.getDriverVersion());
			lConnInfo.put("majorJdbcVersion", meta.getJDBCMajorVersion());
			lConnInfo.put("minorJdbcVersion", meta.getJDBCMinorVersion());
			/*				
			System.out.println("Server name: "
			+ meta.getDatabaseProductName());
			System.out.println("Server version: "
			+ meta.getDatabaseProductVersion());
			System.out.println("Driver name: "
			+ meta.getDriverName());
			System.out.println("Driver version: "
			+ meta.getDriverVersion());
			System.out.println("JDBC major version: "
			+ meta.getJDBCMajorVersion());
			System.out.println("JDBC minor version: "
			+ meta.getJDBCMinorVersion());
			 */
			lResponse.setMap("currentConnection", lConnInfo);
		} catch (Exception lEx) {
		}
		try {
			if (lConn == null) {
				lConn.commit();
				lConn.close();
			}
		} catch (Exception lEx) {
		}
	}
}
