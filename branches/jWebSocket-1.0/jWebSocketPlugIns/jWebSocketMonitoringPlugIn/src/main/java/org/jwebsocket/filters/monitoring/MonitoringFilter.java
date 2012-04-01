/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.filters.monitoring;

import org.jwebsocket.filter.TokenFilter;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.FilterConfiguration;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.token.Token;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;

/**
 *
 * @author Merly
 */
public class MonitoringFilter extends TokenFilter {

	Mongo mConnection;
	DBCollection mColl;
	private static Logger mLog = Logging.getLogger(MonitoringFilter.class);

	public MonitoringFilter(FilterConfiguration aConfig) {

		super(aConfig);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating monitoring filter...");
		}

		try {
			mConnection = new Mongo();
			mColl = mConnection.getDB("db_charting").getCollection("use_plugins");
		} catch (UnknownHostException ex) {
			mLog.error(ex.getMessage());
		}
	}

	@Override
	public void processTokenIn(FilterResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		String lNamespace = aToken.getNS();
		String lPlugInId = null;
		for (WebSocketPlugIn lPlugIn : getServer().getPlugInChain().getPlugIns()) {
			if (lNamespace.equals(lPlugIn.getPluginConfiguration().getNamespace())) {
				lPlugInId = lPlugIn.getId();

				//To save in the database
				DBObject lRecord = mColl.findOne(new BasicDBObject().append("id", lPlugInId));

				if (lRecord == null) {
					mColl.insert(new BasicDBObject().append("id", lPlugInId));
				} else {
					mColl.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("requests", 1)));
				}
			}
		}
	}

	@Override
	public void processTokenOut(FilterResponse aResponse, WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {

		String lNamespace = aToken.getNS();
		String lPlugInId = null;
		for (WebSocketPlugIn lPlugIn : getServer().getPlugInChain().getPlugIns()) {
			if (lNamespace.equals(lPlugIn.getPluginConfiguration().getNamespace())) {
				lPlugInId = lPlugIn.getId();

				//To save in the database
				DBObject lRecord = mColl.findOne(new BasicDBObject().append("id", lPlugInId));

				if (lRecord == null) {
					mColl.insert(new BasicDBObject().append("id", lPlugInId));
				} else {
					mColl.update(lRecord, new BasicDBObject().append("$inc", new BasicDBObject().append("requests", 1)));
				}
			}
		}
	}
}
