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

import java.util.Collection;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 *
 */
public class ExtJSGridFormDemoPlugin extends TokenPlugIn {

	/**
	 *
	 */
	public static final String NS_EXTJSDEMO = 
			JWebSocketServerConstants.NS_BASE + "plugins.extjsgrid";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ExtJSGridFormDemoPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket ExtJSGridFormDemoPlugin - Community Edition";
	private static Customers mCustomers = new Customers();

	/**
	 *
	 * @param aConfiguration
	 */
	public ExtJSGridFormDemoPlugin(PluginConfiguration aConfiguration) {
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
				proccessCreate(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("read")) {
				proccessRead(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("destroy")) {
				proccessDestroy(aResponse, aConnector, aToken);
			} else if (aToken.getType().equals("update")) {
				proccessUpdate(aResponse, aConnector, aToken);
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
		CustomerDef lCustomer = null;

		if ((name != null) && (email != null) && (age != null)) {

			try {
				lCustomer = new CustomerDef(mCustomers.getCount(), name, email, age);
				mCustomers.add(lCustomer);
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
			FastList<CustomerDef> data = new FastList<CustomerDef>();
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

		Token result = createResponse(aToken);
		FastList<CustomerDef> data = new FastList<CustomerDef>();
		Integer id = null;

		try {
			id = Integer.parseInt(aToken.getString("id"));
		} catch (Exception exp) {
		}

		if (id != null) {
			CustomerDef customer = mCustomers.getCustomer(id);
			data.add(customer);

		} else {

			Integer start = aToken.getInteger("start");
			Integer limit = aToken.getInteger("limit");
			result.setList("data", mCustomers.getSubList(start, start + limit));

			result.setInteger("code", 0);
			result.setInteger("totalCount", mCustomers.getSize());
		}
		getServer().sendToken(aConnector, result);

	}

	private void notifyAllConectors(String typeNotify, String message) {

		Token Notify = TokenFactory.createToken("jws.ext.gridformdemo", typeNotify);
		Collection<WebSocketConnector> clients = getServer().getAllConnectors().values();

		Notify.setString("message", message);

		for (WebSocketConnector c : clients) {
			getServer().sendToken(c, Notify);
		}

	}

	private void notifyAllConectorsWithoutMe(WebSocketConnector aConnector, String typeNotify, String message) {

		Token Notify = TokenFactory.createToken("jws.ext.gridformdemo", typeNotify);
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

		CustomerDef customer = mCustomers.getCustomer(id);

		if (customer == null) {
			result.setInteger("code", -1);
			msg = "there is no customer with id " + id + " and name " + name;
		} else {
			customer.setEmail(email);
			customer.setName(name);
			customer.setAge(age);

			msg = "User with id: " + id + " updated correctly";

			FastList<CustomerDef> data = new FastList<CustomerDef>();
			data.add(customer);
			result.setList("data", data);


		}

		result.setString("message", msg);
		getServer().sendToken(aConnector, result);

		if (customer != null) {
			notifyAllConectors("notifyUpdate", msg);
		}
	}

	private void proccessDestroy(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {

		Integer id = aToken.getInteger("id");
		Token result = createResponse(aToken);
		FastList<Token> data = new FastList<Token>();

		if (mCustomers.deleteCustomer(id)) {
			result.setInteger("code", 0);
			result.setBoolean("success", true);
			result.setString("message", "delete success customer with id: " + id);
			result.setList("data", data);
		} else {
			result.setInteger("code", -1);
			result.setBoolean("failure", true);
			result.setString("message", "An error has occurred.  could not delete the user with id: " + id);
		}

		result.setInteger("totalCount", mCustomers.getSize());
		getServer().sendToken(aConnector, result);

		String msg = "delete success customer with id: " + id;
		notifyAllConectors("notifyDestroy", msg);
	}
}
