//	---------------------------------------------------------------------------
//	jWebSocket - jQuery User management Demo Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.jquery;

import java.util.Collection;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
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
public class JQueryDemoPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_JQUERYDEMO
			= JWebSocketServerConstants.NS_BASE + ".plugins.jquerydemo";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket JQueryDemoPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket JQueryDemoPlugIn - Community Edition";
	private static Collection<WebSocketConnector> mClients = new FastList<WebSocketConnector>().shared();
	private static PagedListHolder<User> mUsers = new PagedListHolder<User>();

	/**
	 *
	 * @param aConfiguration
	 */
	public JQueryDemoPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating JQueryDemo plug-in...");
		}
		// specify default name space for JQueryForms plugin
		this.setNamespace(NS_JQUERYDEMO);
		createUsers();
		if (mLog.isInfoEnabled()) {
			mLog.info("JQueryDemo plug-in successfully instantiated.");
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
		return NS_JQUERYDEMO;
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if ("create".equals(aToken.getType())) {
				createUser(aConnector, aToken);
			} else if ("delete".equals(aToken.getType())) {
				deleteUser(aConnector, aToken);
			} else if ("reset".equals(aToken.getType())) {
				resetList(aConnector, aToken);
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
		// EXPECTED PARAMETERS
		String lUserName = aToken.getString("username");
		String lName = aToken.getString("name");
		String lLastName = aToken.getString("lastname");
		String lMail = aToken.getString("mail");

		// VALIDATING INCOMING ARGUMENTS
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
	 *
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

	private void resetList(WebSocketConnector aConnector, Token aToken) {
		//CREATING A RESPONSE TOKEN FOR SENDING THE LIST TO A PRIOR REQUEST
		Token lResult = TokenFactory.createToken(getNamespace(), "resetNotification");
		createUsers();
		String lMsg = "All the changes have been restored by "
				+ aConnector.getId() + "@" + aConnector.getUsername();
		lResult.setString("msg", lMsg);
		broadcast(lResult);
	}

	private void getPage(WebSocketConnector aConnector, Token aToken) {
		//CREATING A RESPONSE TOKEN FOR SENDING THE LIST TO A PRIOR REQUEST
		Token lResult = createResponse(aToken);

		FastList<User> lData = new FastList<User>();

		Integer lCurrentPage = aToken.getInteger("page");

		mUsers.setPage(lCurrentPage);
		mUsers.setPageSize(aToken.getInteger("pagesize"));

		List<User> lPageList = mUsers.getPageList();
		Token ltk;
		FastList<Token> lUsersList = new FastList<Token>();
		for (User lUser : lPageList) {
			ltk = TokenFactory.createToken();
			ltk.setString("name", lUser.getName());
			ltk.setString("lastname", lUser.getLastname());
			ltk.setString("mail", lUser.getMail());
			ltk.setString("username", lUser.getUsername());
			lUsersList.add(ltk);
		}
		//SETTING THE DATA LIST TO THE OUTGOING TOKEN
		lResult.setList("users", lUsersList);
		lResult.setInteger("currentpage", mUsers.getPage());
		lResult.setInteger("maxpages", mUsers.getPageCount());

		getServer().sendToken(aConnector, lResult);
	}

	private void register(WebSocketConnector aConnector) {
		if (!mClients.contains(aConnector)) {
			mClients.add(aConnector);
		}
	}

	private void unregister(WebSocketConnector aConnector) {
		mClients.remove(aConnector);
	}

	private void createUsers() {
		mUsers.getSource().clear();
		mUsers.getSource().add(new User("arojas", "arojash@uci.cu", "Alexander", "Rojas"));
		mUsers.getSource().add(new User("aschulze", "a.schulze@jwebsocket.org", "Alexander", "Schulze"));
		mUsers.getSource().add(new User("anuradha", "galianuradha@gmail.com", "Anuradha", ""));
		mUsers.getSource().add(new User("alsimon", "alsimon@uci.cu", "Armando", "Simon"));
		mUsers.getSource().add(new User("cfeyt", "cfeyt@uci.cu", "Carlos", "Feyt"));
		mUsers.getSource().add(new User("ckcespedes", "ckcespedes@uci.cu", "Carlos", "Karen Céspedes"));
		mUsers.getSource().add(new User("dmederos", "dmederos@hab.uci.cu", "Daimi", "Mederos Llanes"));
		mUsers.getSource().add(new User("dnoa", "dnoa@uci.cu", "Dariel", "Noa Graverán"));
		mUsers.getSource().add(new User("ebouzach", "ebourzach@uci.cu", "Eduardo", "Bouzach"));
		mUsers.getSource().add(new User("johannes", "johannes.schoenborn@gmail.com", "Johannes", "Schoenborn"));
		mUsers.getSource().add(new User("johannes", "j.smutny@gmail.com", "Johannes", "Smutny"));
		mUsers.getSource().add(new User("lzaila", "lzaila@hab.uci.cu", "Lester", "Alfonso Zaila"));
		mUsers.getSource().add(new User("lperez", "lperez@hab.uci.cu", "Lisdey", "Pérez"));
		mUsers.getSource().add(new User("mlopez", "mlopez@hab.uci.cu", "Merly", "López Barroso"));
		mUsers.getSource().add(new User("magonzalez", "magonzalez@hab.uci.cu", "Marcos Antonio", "Gonzalez Huerta"));
		mUsers.getSource().add(new User("mrodriguez", "mrodriguez@hab.uci.cu", "Marta", "Rodríguez Freire"));
		mUsers.getSource().add(new User("memaranon", "memaranon@hab.uci.cu", "Mayra Eva", "Maranon"));
		mUsers.getSource().add(new User("omiranda", "omiranda@uci.cu", "Orlando", "Miranda"));
		mUsers.getSource().add(new User("oaguilar", "oaguilar@uci.cu", "Osvaldo", "Aguilar Lauzurique"));
		mUsers.getSource().add(new User("Prashant", "prashantkhanal@gmail.com", "Prashant", ""));
		mUsers.getSource().add(new User("puran", "mailtopuran@gmail.com", "Puran", "Singh"));
		mUsers.getSource().add(new User("predrag", "stojadinovicp@gmail.com", "Predrag", "Stojadinovic"));
		mUsers.getSource().add(new User("Quentin", "quentin.ambard@gmail.com", "Quentin", "Ambard"));
		mUsers.getSource().add(new User("Rebecca", "r.schulze@jwebsocket.org", "Rebecca", "Schulze"));
		mUsers.getSource().add(new User("rbetancourt", "rbetancourt@hab.uci.cu", "Rolando", "Betancourt"));
		mUsers.getSource().add(new User("rsantamaria", "rsantamaria@jwebsocket.org", "Rolando", "Santamaria"));
		mUsers.getSource().add(new User("rpujol", "rgpujol@hab.uci.cu", "Roylandi", "Pujol"));
		mUsers.getSource().add(new User("Unni", "unnivm@gmail.com", "Unni", ""));
		mUsers.getSource().add(new User("vbarzana", "vbarzana@jwebsocket.org", "Victor Antonio", "Barzana Crespo"));
		mUsers.getSource().add(new User("yvigil", "yvigil@hab.uci.cu", "Yamila", "Vigil Regalado"));
		mUsers.getSource().add(new User("ynunez", "ynbosh@hab.uci.cu", "Yasmani", "Nunez"));
	}

	private void broadcast(Token aToken) {
		for (WebSocketConnector lConnector : mClients) {
			getServer().sendToken(lConnector, aToken);
		}
	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		mClients.remove(aConnector);
	}
}
