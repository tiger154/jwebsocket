//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin (Community Edition, CE)
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
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * SenchaDemoPlugIn. This PlugIn shows a very simple way to integrate Sencha
 * Touch and Sencha Ext JS with jWebSocket Server, both client side integrations
 * use this PlugIn to show the community in a very simple way how to use a
 * jWebSocket communication in their Sencha Applications. In order to avoid
 * notifying all the users of the jWebSocket Server, this PlugIn contains it's
 * own clients list, so, when something happen in the server side, a
 * notification message will only be sent to the registered users.
 *
 * @author Osvaldo Aguilar Lauzurique Lauzurique, Alexander Rojas Hernandez,
 * Victor Antonio Barzana Crespo
 *
 */
public class SenchaDemoPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_SENCHA_DEMO
			= JWebSocketServerConstants.NS_BASE + ".plugins.sencha";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket ExtJSGridFormDemoPlugin";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION
			= "jWebSocket ExtJSGridFormDemoPlugin - Community Edition";
	private static final Collection<WebSocketConnector> mClients
			= new FastList<WebSocketConnector>().shared();
	private static Users mUsers = new Users();

	/**
	 *
	 */
	public static boolean mQuotaAlreadyCreate = false;

	/**
	 * @param aConfiguration
	 */
	public SenchaDemoPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(NS_SENCHA_DEMO);
		if (mLog.isDebugEnabled()) {
			mLog.debug("SenchaDemoPlugIn successfully instantiated!");
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
		return NS_SENCHA_DEMO;
	}

	/**
	 * processToken. Processes all incoming tokens from both Sencha Touch and
	 * Sencha Ext JS if the packets come with the namespace NS_SENCHA_DEMO,
	 * distributes the actions to be made depending on the type of token
	 * received
	 *
	 * @param aResponse
	 * @param aConnector
	 * @param aToken
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (aToken.getNS().equals(getNamespace())) {
			if ("register".equals(aToken.getType())) {
				register(aConnector);
			} else if (aToken.getType().equals("create")) {
				proccessCreate(aConnector, aToken);
			} else if (aToken.getType().equals("read")) {
				proccessRead(aConnector, aToken);
			} else if (aToken.getType().equals("destroy")) {
				proccessDestroy(aConnector, aToken);
			} else if (aToken.getType().equals("update")) {
				proccessUpdate(aConnector, aToken);
			} else if (aToken.getType().equals("reset")) {
				proccessReset(aConnector, aToken);
			}

			//TODO: Remove this, is just for victor quota countDown test
            /*if (!mQuotaAlreadyCreate ){
			 mQuotaAlreadyCreate = true;
			 createQuotas(aConnector);
                
			 }*/
		}
	}

	/**
	 * register. Used mainly for a better performance in the server, the users
	 * interested in this demo will send a token with type register, this client
	 * will be automatically added to an internal connectors reference list, so
	 * when this PlugIn wants to notify the users of any change in the server
	 * side, will do it only to the registered connectors and not to all the
	 * jWebSocket Server connected clients, the clients will unregister in the
	 * connectorStopped method or by sending an unregister tokentype.
	 *
	 * @param aConnector
	 */
	private void register(WebSocketConnector aConnector) {

		if (!mClients.contains(aConnector)) {
			mClients.add(aConnector);
		}
	}

	/**
	 * unregister. The user who doesn't want to receive any other notification
	 * from this PlugIn will send a token to the server with the
	 * type='unregister'
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void unregister(WebSocketConnector aConnector, Token aToken) {
		mClients.remove(aConnector);
	}

	/**
	 * connectorStopped. Whenever the connector leaves jWebSocket Server is
	 * necessary to remove or unregister it from the clients list to avoid
	 * keeping unnecessary references in the clients list. Note: This is very
	 * important to keep in mind, otherwise this will bring big memory
	 * performance problems.
	 *
	 * @param aConnector
	 * @param aCloseReason
	 */
	@Override
	public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
		mClients.remove(aConnector);
	}

//    private void createQuotas(WebSocketConnector aConnector) {
//        /*
//         * Creating Quotas for the create and destroy issues
//         *
//         * The quota create for the crate action is an indirect Quota support
//         * class
//         *
//         * The quota create for the destroy action is a direct Quota support
//         * class
//         */
//        //creating the quota for the create action 
//        try {
//            ActionPlugIn lQPlugin = (ActionPlugIn) getPlugInChain().getPlugIn("jws.quota");
//            Assert.notNull(lQPlugin, "Quota plug-in is not running!");
//
//
//            Token lTokenCreateQuota = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE + ".plugins.quota", "registerQuota");
//
//            lTokenCreateQuota.setString("q_identifier", "CountDown");
//            lTokenCreateQuota.setString("q_value", "5");
//            lTokenCreateQuota.setString("q_instance", "root");
//            lTokenCreateQuota.setString("q_instance_type", "User");
//            lTokenCreateQuota.setString("q_namespace", getNamespace());
//            lTokenCreateQuota.setString("q_actions", "create");
//
//            lQPlugin.invoke(aConnector, lTokenCreateQuota);
//        } catch (Exception exp) {
//            System.out.println("Error creating the quota to the create action ");
//            System.err.println(exp.getMessage());
//        }
//
//        //quotaCountDownDirectSupport
//        try {
//            ActionPlugIn lQPlugin = (ActionPlugIn) getPlugInChain().getPlugIn("jws.quota");
//            Assert.notNull(lQPlugin, "Quota plug-in is not running!");
//
//            Token lTokenDestroyQuota = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE + ".plugins.quota", "registerQuota");
//
//            lTokenDestroyQuota.setString("q_identifier", "CountDownDirectSupport");
//            lTokenDestroyQuota.setString("q_value", "3");
//            lTokenDestroyQuota.setString("q_instance", "root");
//            lTokenDestroyQuota.setString("q_instance_type", "User");
//            lTokenDestroyQuota.setString("q_namespace", getNamespace());
//            lTokenDestroyQuota.setString("q_actions", "destroy");
//
//            lQPlugin.invoke(aConnector, lTokenDestroyQuota);
//        } catch (Exception exp) {
//            System.out.println("Error creating the quota to the destroy action");
//            System.err.println(exp.getMessage());
//        }
//    }
	/**
	 * processCreate. Creates a User in the memory using the data coming in the
	 * token with the following structure: Example: { type: 'create', name:
	 * 'UserX', age: 25, email: "userx@jwebsocket.org" } Fires the captured
	 * exceptions to the client side to notify the users of errors during the
	 * data parsing or other captured error. Submits to the client a response
	 * token as a confirmation that the user was created successfully and
	 * notifies the other clients registered to the demo
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void proccessCreate(
			WebSocketConnector aConnector, Token aToken) {
		try {

			String lName = aToken.getString("name").trim();
			String lEmail = aToken.getString("email").trim();
			Integer lAge = aToken.getInteger("age");

			if (lAge == null) {
				lAge = Integer.parseInt(aToken.getString("age"));
			}

			Token lResult = createResponse(aToken);

			User lUser = new User(mUsers.getCount(), lName, lEmail, lAge);
			mUsers.add(lUser);
			//DATA FOR EXTJS
			lResult.setString("message", "User created successfully!");
			//REACHING DATA FOR SHOWING TO THE USER
			FastList<User> data = new FastList<User>();
			data.add(lUser);
			//SETTING THE DATA LIST TO THE RESPONSE TOKEN
			lResult.setList("data", data);
			//SENDING THE RESPONSE TOKEN
			getServer().sendToken(aConnector, lResult);

			// NOTIFYING THE OTHER USERS OF THE NEW USER CREATED
			String lMsg = "New User created with the following data: "
					+ lUser.toString();
			notifyAllConectors("notifyCreate", lMsg);

		} catch (Exception ex) {
			getServer().sendErrorToken(aConnector, aToken, -1, "The following "
					+ "error was captured in the server: " + ex.getMessage()
					+ ". Please, check your submit data");
		}
	}

	/**
	 * processRead. This method is a simple example of how to send the users to
	 * a previous request from the client side, the list of users will be sent
	 * in a success response with the code = 0, in case that any error is found
	 * the response is always sent with code=-1. This is a very important method
	 * mainly used by the proxies to get the data from the server, each
	 * Ext.jws.data.Proxy will include all the parameters to load the data from
	 * the server and this method should support all these parameters, for
	 * example: 1- The proxy in the client side can be used by a store and this
	 * store can - have a pagination limit: start: 0, limit: 20 Also the
	 * response for this method can vary depending on the component that is
	 * using the jWebSocket proxy, that's why the implementation of this method
	 * can be as big as the user wants.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void proccessRead(
			WebSocketConnector aConnector, Token aToken) {
		try {
			Token lResult = createResponse(aToken);
			FastList<Token> lResultList = new FastList<Token>();
			Integer lId = aToken.getInteger("id");

			if (lId != null) {
				User lUser = mUsers.getCustomer(lId);
				if (lUser != null) {
					Token lAuxToken = TokenFactory.createToken();
					lUser.writeToToken(lAuxToken);
					lResultList.add(lAuxToken);
				} else {
					lResult.setString("msg", "The user with id: " + lId
							+ " does not exist in the server");
					lResult.setCode(-1);
				}
			} else {
				Integer lStart = aToken.getInteger("start");
				Integer lLimit = aToken.getInteger("limit");
				List<User> lUsersList;
				if (lStart != null && lLimit != null) {
					lUsersList = mUsers.getSubList(lStart, lStart + lLimit);
				} else {
					lUsersList = mUsers.getCustomers();
				}
				for (User lUser : lUsersList) {
					Token lAuxToken = TokenFactory.createToken();
					lUser.writeToToken(lAuxToken);
					lResultList.add(lAuxToken);
				}
				lResult.setInteger("totalCount", mUsers.getSize());
			}

			lResult.setList("data", lResultList);

			getServer().sendToken(aConnector, lResult);
		} catch (Exception aException) {
			getServer().sendErrorToken(aConnector, aToken, -1, "The following "
					+ "error was captured in the server: " + aException.getMessage());
		}
	}

	/**
	 * processUpdate. Updates an user by a given id, with the parameters email,
	 * name, age, these parameters, as well as the id, are required. Returns a
	 * success token to the sender and sends to all registered clients a user
	 * update notification.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void proccessUpdate(
			WebSocketConnector aConnector, Token aToken) {
		try {
			Token lResult = createResponse(aToken);
			String lMsg,
					lName = aToken.getString("name"),
					lEmail = aToken.getString("email");

			Integer lUserId = aToken.getInteger("id");
			Integer lAge = aToken.getInteger("age");

			if (lUserId == null) {
				lUserId = Integer.parseInt(aToken.getString("id"));
			}
			if (lAge == null) {
				lAge = Integer.parseInt(aToken.getString("age"));
			}

			User lUser = mUsers.getCustomer(lUserId);

			if (lUser == null) {
				lResult.setInteger("code", -1);
				lMsg = "there is no customer with id " + lUserId + " and name " + lName;
			} else {
				lUser.setEmail(lEmail);
				lUser.setName(lName);
				lUser.setAge(lAge);

				lMsg = "User with id: " + lUserId + " updated correctly";

				FastList<User> lData = new FastList<User>();
				lData.add(lUser);
				lResult.setList("data", lData);
			}

			lResult.setString("message", lMsg);
			getServer().sendToken(aConnector, lResult);

			if (lUser != null) {
				notifyAllConectors("notifyUpdate", lMsg);
			}
		} catch (NumberFormatException ex) {
			getServer().sendErrorToken(aConnector, aToken, -1, "The following "
					+ "error was captured in the server: " + ex.getMessage()
					+ ". Please, check your submit data");
		}
	}

	/**
	 * processDestroy. Removes an user from the list, requires a user id,
	 * notifies all the registered clients with the user deleted.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void proccessDestroy(WebSocketConnector aConnector, Token aToken) {
		try {
			Integer lUserId = aToken.getInteger("id");
			Token lResult = createResponse(aToken);
			FastList<Token> lData = new FastList<Token>();

			/*
			 ActionPlugIn lQPlugin = (ActionPlugIn) getPlugInChain().getPlugIn("jws.quota");
			 Assert.notNull(lQPlugin, "Quota plug-in is not running!");

			 Token lTokenDestroyQuota = TokenFactory.createToken(JWebSocketServerConstants.NS_BASE + ".plugins.quota", "reduceQuota");
			 lTokenDestroyQuota.setString("q_identifier", "CountDownDirectSupport");
			 lTokenDestroyQuota.setString("q_value", "1");
			 lTokenDestroyQuota.setString("q_instance", "root");
			 lTokenDestroyQuota.setString("q_instance_type", "User");
			 lTokenDestroyQuota.setString("q_namespace", getNamespace());


			 Token lToken = lQPlugin.invoke(aConnector, lTokenDestroyQuota);
			 if (lToken.getCode() == -1) {
			 Do wherever you want with quota exceed for example do not delete
               
			 }
			 */
			if (mUsers.deleteCustomer(lUserId)) {

				lResult.setInteger("code", 0);
				lResult.setBoolean("success", true);
				lResult.setString("message", "User deleted successfully with id: "
						+ lUserId);

				lResult.setList("data", lData);
			} else {
				lResult.setInteger("code", -1);
				lResult.setBoolean("failure", true);
				lResult.setString("message",
						"An error has occurred, the user with id: "
						+ lUserId + " could not be deleted");
			}

			lResult.setInteger("totalCount", mUsers.getSize());
			getServer().sendToken(aConnector, lResult);

			String lMsg = "User with id: " + lUserId + " has been deleted from the server";
			notifyAllConectors("notifyDestroy", lMsg);
		} catch (Exception aException) {
			getServer().sendErrorToken(aConnector, aToken, -1, "The following "
					+ "error was captured in the server: " + aException.getMessage()
					+ ". Please, check your submit data");
		}
	}

	/**
	 * processReset. Resets the list to its original state and notifies all the
	 * registered clients that the list is modified, so, if they are interested
	 * they will request again the new modified list.
	 *
	 * @param aConnector
	 * @param aToken
	 */
	private void proccessReset(WebSocketConnector aConnector, Token aToken) {
		mUsers = new Users();
		String lMsg = "All the changes have been restored by "
				+ aConnector.getId() + "@" + aConnector.getUsername();
		notifyAllConectors("notifyReset", lMsg);
	}

	/**
	 * notifyAllConnectors. Notifies all the connectors with a string message,
	 * this method is a helper for the current PlugIn, the users can also use
	 * the method getServer().broadcastToken(lNotificationToken), but this
	 * method will notify to all jWebSocket connected clients.
	 *
	 * @param aNotificationType
	 * @param aMessage
	 */
	private void notifyAllConectors(String aNotificationType, String aMessage) {
		Token lNotificationToken = TokenFactory.createToken(NS_SENCHA_DEMO,
				aNotificationType);

		lNotificationToken.setString("message", aMessage);
		for (WebSocketConnector lConnector : mClients) {
			getServer().sendToken(lConnector, lNotificationToken);
		}
	}
}
