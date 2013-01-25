//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket JQuery User management Demo Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.jquery;

import java.util.Collection;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.support.PagedListHolder;

/**
 * @author Victor Antonio Barzana Crespo
 */
public class JQueryFormsPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger();
	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>().shared();
	private static PagedListHolder<User> mUsers = new PagedListHolder<User>();

	public JQueryFormsPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JQueryForms plug-in...");
		}
		// specify default name space for JQueryForms plugin
		this.setNamespace(aConfiguration.getNamespace());
		createUsers();
		if (mLog.isInfoEnabled()) {
			mLog.info("JQueryForms plug-in successfully loaded.");
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if ("create".equals(aToken.getType())) {
				createUser(aConnector, aToken);
			} else if ("delete".equals(aToken.getType())) {
				deleteUser(aConnector, aToken);
			} else if ("getall".equals(aToken.getType())) {
				getUsers(aConnector, aToken);
			} else if ("getpage".equals(aToken.getType())) {
				getPage(aConnector, aToken);
			} else if ("register".equals(aToken.getType())) {
				register(aConnector);
			} else if ("unregister".equals(aToken.getType())) {
				unregister(aConnector);
			}
		}
	}

	private void createUser(WebSocketConnector aConnector, Token aToken) {
		//EXPECTED PARAMETERS
		String lUserName = aToken.getString("username");
		String lName = aToken.getString("name");
		String lLastName = aToken.getString("lastname");
		String lMail = aToken.getString("mail"); 

		//VALIDATING INCOMING ARGUMENTS
		if ((lUserName != null) && (lName != null) && (lLastName != null)) {
			if (!(lUserName.trim().equals("")) && !(lLastName.trim().equals(""))
					&& !(lName.trim().equals(""))) {
				Token lResult = TokenFactory.createToken(getNamespace(), "usercreated");
				User lUser = new User(lUserName, lMail, lName, lLastName);
				if (mUsers.getSource().contains(lUser)) {
					getServer().sendErrorToken(aConnector, aToken, -1, "Please check parameters, the user already exist");
					return;
				}
				mUsers.getSource().add(lUser);
				lResult.setString("name", lName);
				lResult.setString("username", lUserName);
				lResult.setString("lastname", lLastName);
				lResult.setString("mail", lMail);

				// Notify all clients of the new user created
				broadcast(lResult);
			} else {
				getServer().sendErrorToken(aConnector, aToken, -1, "Please check parameters, \"Username\", \"Name\" or \"Lastname\" empty");
			}
		} else {
			getServer().sendErrorToken(aConnector, aToken, -1, "Expected parameter: \"Username\", \"Name\" or \"Lastname\" not sent to the server");
		}
	}

	private void deleteUser(WebSocketConnector aConnector, Token aToken) {
		String lUsername = aToken.getString("username");
		mUsers.getSource().remove(new User(lUsername, null, null, null));
		Token lToken = TokenFactory.createToken(getNamespace(), "userdeleted");
		lToken.setString("username", lUsername);
		broadcast(lToken);
	}

	/**
	 * Returns all Users
	 * @param aConnector
	 * @param aToken 
	 */
	private void getUsers(WebSocketConnector aConnector, Token aToken) {
		//CREATING A RESPONSE TOKEN FOR SENDING THE LIST TO A PRIOR REQUEST
		Token lResult = createResponse(aToken);

		//SETTING THE DATA LIST TO THE OUTGOING TOKEN
		lResult.setList("users", mUsers.getSource());

		getServer().sendToken(aConnector, lResult);
	}

	private void getPage(WebSocketConnector aConnector, Token aToken) {
		//CREATING A RESPONSE TOKEN FOR SENDING THE LIST TO A PRIOR REQUEST
		Token lResult = createResponse(aToken);

		FastList<User> lData = new FastList<User>();

		Integer lCurrentPage = aToken.getInteger("page");

		mUsers.setPage(lCurrentPage);
		mUsers.setPageSize(aToken.getInteger("pagesize"));

		List<User> lPageList = mUsers.getPageList();
		//SETTING THE DATA LIST TO THE OUTGOING TOKEN
		lResult.setList("users", lPageList);
		lResult.setInteger("currentpage", mUsers.getPage());
		lResult.setInteger("maxpages", mUsers.getPageCount());

		getServer().sendToken(aConnector, lResult);
	}

	private void register(WebSocketConnector aConnector) {
		mClients.add(aConnector);
	}

	private void unregister(WebSocketConnector aConnector) {
		mClients.remove(aConnector);
	}

	private void createUsers() {
		mUsers.getSource().add(new User("aschulze", "a.schulze@jwebsocket.org", "Alexander", "Schulze"));
		mUsers.getSource().add(new User("rsantamaria", "rsantamaria@jwebsocket.org", "Rolando", "Santamaria"));
		mUsers.getSource().add(new User("vbarzana", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana1", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana2", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana3", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana4", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana5", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana6", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana7", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana8", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("vbarzana9", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
	}

	private void broadcast(Token aToken) {
		for (WebSocketConnector lConnector : mClients) {
			getServer().sendToken(lConnector, aToken);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		if(mClients.contains(aConnector)){
			mClients.remove(aConnector);
		}
	}
}