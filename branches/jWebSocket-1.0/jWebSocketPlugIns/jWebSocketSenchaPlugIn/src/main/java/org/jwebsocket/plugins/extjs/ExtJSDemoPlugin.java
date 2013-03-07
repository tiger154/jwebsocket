//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin (Community Edition, CE)
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
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class ExtJSDemoPlugin extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	public static final String NS_EXTJSDEMO =
			JWebSocketServerConstants.NS_BASE + ".plugins.sencha";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ExtJSDemoPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ExtJSDemoPlugin - Community Edition";
	private static Users mUsers = new Users();

	/**
	 *
	 * @param aConfiguration
	 */
	public ExtJSDemoPlugin(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS_EXTJSDEMO);
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
		return NS_EXTJSDEMO;
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals("create")) {
				processCreate(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("update")) {
				processUpdate(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("read")) {
				processRead(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("destroy")) {
				processDestroy(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("getAllUsers")) {
				getAllUsers(aResponse, aConnector, aToken);
			}
		}
	}

	private void processCreate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		String name = aToken.getString("name");
		String email = aToken.getString("email");

		int lenght = mUsers.getUsers().size();
		Token result = createResponse(aToken);

		if ((name != null) && (email != null)) {
			if ((name.trim().equals("")) || (email.trim().equals(""))) {
				getServer().sendErrorToken(aConnector, aToken, -1, "Campo nombre no dado");
			} else {
				UserDef userToAdd = new UserDef(mUsers.getCount(), name, email);
				try {
					mUsers.add(userToAdd);
					result.setInteger("code", 0);
					result.setString("message", "User created correctly");

				} catch (Exception ex) {

					result.setString("message", ex.getMessage());
					result.setInteger("code", -1);
				}

				//REACHING DATA FOR SHOWING TO THE USER
				FastList<Token> data = new FastList<Token>();
				Token ta = TokenFactory.createToken();
				ta.setInteger("id", userToAdd.getId());
				ta.setString("name", name);
				ta.setString("email", email);
				data.add(ta);

				//SETTING THE DATA LIST TO THE OUTGOING TOKEN
				result.setList("data", data);

				//SENDING THE TOKEN
				getServer().sendToken(aConnector, result);
			}
		} else {
			getServer().sendErrorToken(aConnector, aToken, -1, "Campo nombre no dado");
		}

	}

	private void processUpdate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		String name = aToken.getString("name");
		String email = aToken.getString("email");
		Integer id = aToken.getInteger("id");

		Token result = createResponse(aToken);

		UserDef user = mUsers.getUser(id);

		if (user != null) {
			user.setEmail(email);
			user.setName(name);


			result.setInteger("code", 0);
			result.setString("message", "User with id: " + id + " updated correctly");

			//REACHING DATA FOR SHOWING TO THE USER
			FastList<Token> data = new FastList<Token>();
			Token ta = TokenFactory.createToken();
			ta.setString("name", user.getName());
			ta.setString("email", user.getEmail());
			ta.setInteger("id", user.getId());
			data.add(ta);


			result.setList("data", data);
		} else {
			result.setInteger("code", -1);
			result.setString("message", "An error has occurred.  could not update the user with id: " + id);
		}
		getServer().sendToken(aConnector, result);
	}

	private void processRead(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		Integer id = Integer.parseInt(aToken.getString("id"));
		UserDef user = mUsers.getUser(id);
		Token result = createResponse(aToken);

		FastList<Token> data = new FastList<Token>();

		if (user != null) {

			result.setInteger("code", 0);
			result.setString("message", "User found with id: " + id);

			Token ta = TokenFactory.createToken();
			ta.setString("name", user.getName());
			ta.setString("email", user.getEmail());
			ta.setInteger("id", user.getId());
			data.add(ta);
		} else {
			result.setInteger("code", -1);
			result.setString("message", "there is no customer with id: " + id);
		}

		result.setList("data", data);

		getServer().sendToken(aConnector, result);
	}

	private void processDestroy(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		Integer id = aToken.getInteger("id");
		Token result = createResponse(aToken);

		FastList<Token> data = new FastList<Token>();

		if (mUsers.deleteUser(id)) {
			result.setInteger("code", 0);
			result.setString("message", "delete success customer with id: " + id);
			result.setList("data", data);
		} else {
			result.setInteger("code", -1);
			result.setString("message", "An error has occurred.  could not delete the user with id: " + id);
		}

		getServer().sendToken(aConnector, result);
	}

	private void getAllUsers(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		LinkedList<UserDef> users = mUsers.getUsers();
		FastList<Token> data = new FastList<Token>();

		Token result = createResponse(aToken);

		if (users.size() > 0) {

			for (UserDef ud : users) {

				Token ta = TokenFactory.createToken();
				ta.setString("name", ud.getName());
				ta.setString("email", ud.getEmail());
				ta.setInteger("id", ud.getId());
				data.add(ta);
			}

			result.setInteger("code", 0);
			result.setList("data", data);

		} else {
			result.setInteger("code", -1);
			result.setString("message", "has not created any customer");
		}

		getServer().sendToken(aConnector, result);
	}
}
