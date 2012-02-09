//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Alexander Schulze,
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
package org.jwebsocket.plugins.extjs;

import java.util.Collection;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
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

	private static Customers cutomersList = new Customers();

	public ExtJSGridFormDemoPlugin(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
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
				lCustomer = new CustomerDef(cutomersList.getCount(), name, email, age);
				cutomersList.add(lCustomer);
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
			CustomerDef customer = cutomersList.getCustomer(id);
			data.add(customer);

		} else {

			Integer start = aToken.getInteger("start");
			Integer limit = aToken.getInteger("limit");
			result.setList("data", cutomersList.getSubList(start, start + limit));

			result.setInteger("code", 0);
			result.setInteger("totalCount", cutomersList.getSize());
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

		CustomerDef customer = cutomersList.getCustomer(id);

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

		if (cutomersList.deleteCustomer(id)) {
			result.setInteger("code", 0);
			result.setBoolean("success", true);
			result.setString("message", "delete success customer with id: " + id);
			result.setList("data", data);
		} else {
			result.setInteger("code", -1);
			result.setBoolean("failure", true);
			result.setString("message", "An error has occurred.  could not delete the user with id: " + id);
		}

		result.setInteger("totalCount", cutomersList.getSize());
		getServer().sendToken(aConnector, result);

		String msg = "delete success customer with id: " + id;
		notifyAllConectors("notifyDestroy", msg);
	}
}
