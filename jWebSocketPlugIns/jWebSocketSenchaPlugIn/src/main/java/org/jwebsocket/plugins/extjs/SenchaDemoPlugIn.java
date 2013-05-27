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

import org.jwebsocket.plugins.extjs.Util.User;
import org.jwebsocket.plugins.extjs.Util.Users;
import java.util.Collection;
import java.util.List;
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
 * @author Osvaldo Aguilar Lauzurique, (oaguilar, La Habana), Alexander Rojas Hernandez (arojas, Pinar del Rio), Victor Antonio Barzana Crespo (vbarzana, Münster Westfalen)
 *
 */
public class SenchaDemoPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	public static final String NS_EXTJSDEMO =
			JWebSocketServerConstants.NS_BASE + ".plugins.sencha";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ExtJSGridFormDemoPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ExtJSGridFormDemoPlugin - Community Edition";
	private static Users mUsers = new Users();

	/**
	 *
	 * @param aConfiguration
	 */
	public SenchaDemoPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS_EXTJSDEMO);
		if (mLog.isDebugEnabled()) {
			mLog.debug("ExtJSGridFormDemoPlugin successfully instantiated!");
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
		return NS_EXTJSDEMO;
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		if (aToken.getNS().equals(getNamespace())) {
			if (aToken.getType().equals("create")) {
				proccessCreate(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("read")) {
				proccessRead(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("destroy")) {
				proccessDestroy(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("update")) {
				proccessUpdate(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("reset")) {
				proccessReset(aResponse, aConnector, aToken);
			}

		}
	}

	private void proccessCreate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		String name = aToken.getString("name").trim();
		String email = aToken.getString("email").trim();
		Integer age = 0;

		age = aToken.getInteger("age");

		if (age == null) {
			age = Integer.parseInt(aToken.getString("age"));
		}

		Token result = createResponse(aToken);
		User lCustomer = null;

		if ((name != null) && (email != null) && (age != null)) {

			try {
				lCustomer = new User(mUsers.getCount(), name, email, age);
				mUsers.add(lCustomer);
				//{"success":true,"message":"User Create","data":   {"id":5,"name":"osvaldo","email":"oaguilar@rubble.com"}}
				//DATA FOR EXTJS
				result.setInteger("code", 0);
				result.setBoolean("success", true);
				result.setString("message", "User with mail: " + email + " was created correctly");

			} catch (Exception ex) {

				result.setString("message", ex.getMessage());
				result.setInteger("code", -1);
				result.setBoolean("failure", true);
			}
			//REACHING DATA FOR SHOWING TO THE USER
			FastList<User> data = new FastList<User>();
			data.add(lCustomer);

			//SETTING THE DATA LIST TO THE OUTGOING TOKEN
			result.setList("data", data);

			//SENDING THE TOKEN
			getServer().sendToken(aConnector, result);

			String msg = "User with mail: " + email + " was created correctly";
			notifyAllConectors("notifyCreate", msg);

		} else {
			getServer().sendErrorToken(aConnector, aToken, -1, "one of them name, email, age are undefined ");
		}

	}

	private void proccessRead(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		Token lResult = createResponse(aToken);
		FastList<Token> lResultList = new FastList<Token>();
		Integer lId = aToken.getInteger("id");

		if (lId != null) {
			User lCustomer = mUsers.getCustomer(lId);
			if (lCustomer != null) {
				Token lTk = TokenFactory.createToken();
				lCustomer.writeToToken(lTk);
				lResultList.add(lTk);
			} else {
				lResult.setString("msg", "The user with id: " + lId + " does not exist in the server");
				lResult.setCode(-1);
			}
		} else {
			Integer lStart = aToken.getInteger("start");
			Integer lLimit = aToken.getInteger("limit");
			List<User> lUsersList;
			if(lStart != null && lLimit != null ) {
				lUsersList = mUsers.getSubList(lStart, lStart + lLimit);
			} else {
				lUsersList = mUsers.getCustomers();
			}

			for (User lCustomer : lUsersList) {
				Token lTk = TokenFactory.createToken();
				lCustomer.writeToToken(lTk);
				lResultList.add(lTk);
			}
			lResult.setInteger("totalCount", mUsers.getSize());
		}
		
		lResult.setList("data", lResultList);
		
		getServer().sendToken(aConnector, lResult);

	}

	private void notifyAllConectors(String typeNotify, String message) {

		Token Notify = TokenFactory.createToken(NS_EXTJSDEMO, typeNotify);
		Collection<WebSocketConnector> clients = getServer().getAllConnectors().values();

		Notify.setString("message", message);

		for (WebSocketConnector c : clients) {
			getServer().sendToken(c, Notify);
		}

	}

	private void notifyAllConectorsWithoutMe(WebSocketConnector aConnector, String typeNotify, String message) {

		Token Notify = TokenFactory.createToken(NS_EXTJSDEMO, typeNotify);
		Collection<WebSocketConnector> clients = getServer().getAllConnectors().values();
		clients.remove(aConnector);

		Notify.setString("message", message);

		for (WebSocketConnector c : clients) {
			getServer().sendToken(c, Notify);
		}

	}

	private void proccessUpdate(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		Token result = createResponse(aToken);
		String msg = "";

		String name = aToken.getString("name");
		String email = aToken.getString("email");
		Integer id = 0;
		Integer age = 0;

		id = aToken.getInteger("id");
		age = aToken.getInteger("age");

		if (id == null) {
			id = Integer.parseInt(aToken.getString("id"));
		}
		if (age == null) {
			age = Integer.parseInt(aToken.getString("age"));
		}

		User lCustomer = mUsers.getCustomer(id);

		if (lCustomer == null) {
			result.setInteger("code", -1);
			msg = "there is no customer with id " + id + " and name " + name;
		} else {
			lCustomer.setEmail(email);
			lCustomer.setName(name);
			lCustomer.setAge(age);

			msg = "User with id: " + id + " updated correctly";

			FastList<User> data = new FastList<User>();
			data.add(lCustomer);
			result.setList("data", data);
		}

		result.setString("message", msg);
		getServer().sendToken(aConnector, result);

		if (lCustomer != null) {
			notifyAllConectors("notifyUpdate", msg);
		}
	}

	private void proccessDestroy(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		Integer id = aToken.getInteger("id");
		Token result = createResponse(aToken);
		FastList<Token> data = new FastList<Token>();

		if (mUsers.deleteCustomer(id)) {
			result.setInteger("code", 0);
			result.setBoolean("success", true);
			result.setString("message", "delete success customer with id: " + id);
			result.setList("data", data);
		} else {
			result.setInteger("code", -1);
			result.setBoolean("failure", true);
			result.setString("message", "An error has occurred.  could not delete the user with id: " + id);
		}

		result.setInteger("totalCount", mUsers.getSize());
		getServer().sendToken(aConnector, result);

		String msg = "delete success customer with id: " + id;
		notifyAllConectors("notifyDestroy", msg);
	}
	
	private void proccessReset(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		mUsers = new Users();
		String msg = "All the changes have been restored by " + aConnector.getId() + "@" + aConnector.getUsername();
		notifyAllConectors("notifyReset", msg);
	}
}
