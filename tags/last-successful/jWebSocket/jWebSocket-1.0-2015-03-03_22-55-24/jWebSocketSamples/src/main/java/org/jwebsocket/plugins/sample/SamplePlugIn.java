//	---------------------------------------------------------------------------
//	jWebSocket Sample Plug-in (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
package org.jwebsocket.plugins.sample;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;
import org.springframework.util.Assert;

/**
 *
 * @author Alexander Schulze
 */
public class SamplePlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	// if namespace changed update client plug-in accordingly!
	private final static String NS_SAMPLE = JWebSocketServerConstants.NS_BASE + ".plugins.samples";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SamplePlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket SamplePlugIn - Community Edition";
	private final static String SAMPLE_VAR = NS_SAMPLE + ".started";

	/**
	 *
	 * @param aConfiguration
	 */
	public SamplePlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating sample plug-in...");
		}
		// specify default name space for sample plugin
		this.setNamespace(NS_SAMPLE);
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
	public void connectorStarted(WebSocketConnector aConnector) {
		// this method is called every time when a client
		// connected to the server
		aConnector.setVar(SAMPLE_VAR, new Date().toString());
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		// this method is called every time when a client
		// disconnected from the server
	}

	/**
	 * Gets the JDBC plugin default database connection
	 *
	 * @param aAlias
	 * @return
	 * @throws Exception
	 */
	private Connection getJDBCConnection(String aAlias) throws Exception {
		TokenPlugIn lJDBCPlugIn = (TokenPlugIn) getServer().getPlugInById("jws.jdbc");
		Assert.notNull(lJDBCPlugIn, "The ReportingPlugin required JDBC plug-in enabled!");

		Token lRequest = TokenFactory.createToken();
		lRequest.setString("alias", aAlias);

		Object lObject = Tools.invoke(lJDBCPlugIn, "getNativeDataSource", new Class[]{Token.class}, lRequest);
		DataSource lDataSource = (DataSource) lObject;

		return lDataSource.getConnection();
	}

	@Override
	public void systemStarted() throws Exception {
		Connection lConnection = getJDBCConnection(null);
		try {
			lConnection.prepareStatement(
					"CREATE TABLE jwebsocket_reporting_demo \n"
					+ "(\n"
					+ "  user_id int,\n"
					+ "  name varchar(255),\n"
					+ "  lastName varchar(255),\n"
					+ "  age int,\n"
					+ "  email varchar(255)\n"
					+ ")\n").execute();
			lConnection.prepareStatement(
					"INSERT INTO jwebsocket_reporting_demo (user_id, name, lastName, age, email) VALUES\n"
					+ "(1, 'Alexander', 'Schulze', 45, 'a.schulze@jwebsocket.org'),\n"
					+ "(2, 'Rolando', 'Santamaria', 28,'rsantamaria@jwebsocket.org'),\n"
					+ "(3, 'Lisdey', 'Perez', 27, 'lperez@jwebsocket.org'),\n"
					+ "(4, 'Marcos', 'Gonzalez', 27, 'mgonzalez@jwebsocket.org'),\n"
					+ "(5, 'Osvaldo', 'Aguilar', 27, 'oaguilar@jwebsocket.org'),\n"
					+ "(6, 'Victor', 'Barzana', 27, 'vbarzana@jwebsocket.org'),\n"
					+ "(7, 'Javier', 'Puentes', 26, 'jpuentes@jwebsocket.org')").execute();

			if (mLog.isDebugEnabled()) {
				mLog.debug("JDBC 'jwebsocket_reporting_demo' database table for demos has been "
						+ "created with sample data in JDBCPlugIn 'default' alias.");
			}
		} catch (SQLException lEx) {
			// DO NOT CAPTURE, table already exists!!!
		}
	}

	@Override
	public void engineStarted(WebSocketEngine aEngine) {
		WebSocketServerTokenListener lListener = new WebSocketServerTokenListener() {
			@Override
			public void processToken(WebSocketServerTokenEvent aEvent, Token aToken) {
				if (NS_SAMPLE.equals(aToken.getNS())
						&& "getCopyrightAndLicense".equals(aToken.getType())) {
					Token lResponse = createResponse(aToken);
					lResponse.setString("copyright", getCopyright());
					lResponse.setString("license", getLicense());

					sendToken(aEvent.getConnector(), lResponse);
				}
			}

			@Override
			public void processOpened(WebSocketServerEvent aEvent) {
			}

			@Override
			public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket) {
			}

			@Override
			public void processClosed(WebSocketServerEvent aEvent) {
			}
		};

		if (!getServer().getListeners().contains(lListener)) {
			getServer().addListener(lListener);
		}

		// this method is called when the engine has started
		super.engineStarted(aEngine);
	}

	@Override
	public void engineStopped(WebSocketEngine aEngine) {
		// this method is called when the engine has stopped
		super.engineStopped(aEngine);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		// get the type of the token
		// the type can be associated with a "command"
		String lType = aToken.getType();

		// get the namespace of the token
		// each plug-in should have its own unique namespace
		String lNS = aToken.getNS();

		// check if token has a type and a matching namespace
		if (lType != null && lNS != null && lNS.equals(getNamespace())) {

			// get the server time
			if ("requestServerTime".equals(lType)) {
				requestServerTime(aConnector, aToken);
			} else if (lType.equals("processComplexObject")) {
				processComplexObject(aConnector, aToken);
			} else if (lType.equals("getTokenizable")) {
				getTokenizable(aConnector, aToken);
			} else if (lType.equals("getRandom")) {
				getRandom(aConnector, aToken);
			}
		}
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void requestServerTime(WebSocketConnector aConnector, Token aToken) {
		// create the response token
		// this includes the unique token-id
		Token lResponse = createResponse(aToken);

		// add the "time" and "started" field
		lResponse.setString("time", new Date().toString());
		lResponse.setString("started", (String) aConnector.getVar(SAMPLE_VAR));

		// send the response token back to the client
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void processComplexObject(WebSocketConnector aConnector, Token aToken) {
		// simply echo the complex object
		sendToken(aConnector, aConnector, aToken);
	}

	/**
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getRandom(WebSocketConnector aConnector, Token aToken) {
		// create the response token
		Token lResponse = createResponse(aToken);

		// add the random number
		lResponse.setDouble("random", Math.random());

		// send the response token back to the client
		sendToken(aConnector, aConnector, lResponse);
	}

	class Person implements ITokenizable {

		private final String lName;
		private final String lEmail;

		public Person(String aName, String aEmail) {
			this.lName = aName;
			this.lEmail = aEmail;
		}

		@Override
		public void readFromToken(Token aToken) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void writeToToken(Token aToken) {
			aToken.setString("name", lName);
			aToken.setString("email", lEmail);
		}
	}

	private void getTokenizable(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		lResponse.setToken("data", new Person("Rolando SM", "rsantamaria@jwebsocket.org"));

		sendToken(aConnector, lResponse);
	}
}
