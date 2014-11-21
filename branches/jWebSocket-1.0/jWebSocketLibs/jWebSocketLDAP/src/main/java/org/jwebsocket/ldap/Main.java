//	---------------------------------------------------------------------------
//	jWebSocket - LDAP Test App (Community Edition, CE)
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
package org.jwebsocket.ldap;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javolution.util.FastList;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Alexander Schulze
 */
public class Main {

	private static final Logger mLog = Logger.getLogger(ADTools.class);
	// URL to AD Server
	private static String LDAP_URL = "ldap://<yourLDAPHost>:389";
	// DN to start search for groups
	private static String BASE_DN_GROUPS = "OU=Groups,DC=<yourCompany>,DC=<yourTLD>";
	// DN to start search for users
	private static String BASE_DN_USERS = "OU=Users,OU=Accounts,DC=<yourCompany>,DC=<yourTLD>";

	private static String USERNAME = "<yourUsername>@<yourCompany>.<yourTLD>";
	private static String PASSWORD = "<yourPassword>";

	private static String mTestDL0 = "<Name of test DL 0, not DN>";
	private static String mTestDL1 = "<Name of testDL 1, not DN>";
	private static String[] mTestUserNames = {"user0", "user1", "user2", "user3"};

	private static ADTools lADTools;

	private static void init() {
		// set your values here...
		LDAP_URL = "...";
		BASE_DN_GROUPS = "...";
		BASE_DN_USERS = "...";
		USERNAME = "...";
		PASSWORD = "...";
		mTestDL0 = "...";
		mTestDL1 = "...";
		mTestUserNames = new String[]{"...", "...", "...", "..."};

		mLog.info("jWebSocket LDAP examples");
		Configuration lConfig = null;
		boolean lConfigLoaded;
		try {
			// loading properties files
			lConfig = new PropertiesConfiguration("private.properties");
		} catch (ConfigurationException ex) {
		}

		if (null == lConfig) {
			mLog.info("Configuration file could not be opened.");
			return;
		}

		LDAP_URL = lConfig.getString("LDAPURL");
		BASE_DN_GROUPS = lConfig.getString("BaseDNGroups");
		BASE_DN_USERS = lConfig.getString("BaseDNUsers");
		USERNAME = lConfig.getString("BindUsername");
		PASSWORD = lConfig.getString("BindPassword");
		mTestDL0 = lConfig.getString("TestDL0");
		mTestDL1 = lConfig.getString("TestDL1");
		mTestUserNames = lConfig.getStringArray("TestUserNames");

		// TODO: Validate config data here!
		lConfigLoaded = true;

		if (!lConfigLoaded) {
			mLog.error("Config not loaded.");
			System.exit(1);
		}
	}

	/**
	 *
	 * @param aAD
	 */
	public static void demo(ADTools aAD) {

		aAD.login(USERNAME, PASSWORD);

		if (aAD.getDirContext() != null) {
			String lDNDL0 = aAD.getDNfromDL(mTestDL0);
			mLog.info("DN User " + mTestDL0 + ": " + lDNDL0);

			String lDN_User0 = aAD.getDNfromLoginname(mTestUserNames[0]);
			mLog.info("DN User '" + mTestUserNames[0] + "': " + lDN_User0);
			String lDN_User1 = aAD.getDNfromLoginname(mTestUserNames[1]);
			mLog.info("DN User '" + mTestUserNames[1] + "': " + lDN_User1);
			String lDN_User2 = aAD.getDNfromLoginname(mTestUserNames[2]);
			mLog.info("DN User '" + mTestUserNames[2] + "': " + lDN_User2);
			String lDN_User3 = aAD.getDNfromLoginname(mTestUserNames[3]);
			mLog.info("DN User '" + mTestUserNames[3] + "': " + lDN_User3);

			String lDNDL1 = aAD.getDNfromDL(mTestDL1);
			mLog.info("DN DL '" + mTestDL1 + "': " + lDNDL1);

			Map<String, String> lDNs;

			String[] lAttrs = {"distinguishedName", "displayName", "mail", "mailNickname"};

			String lTestUserId = mTestUserNames[0];

			Map<String, List<String>> lTmpRes = aAD.getUserAttrs(lTestUserId, lAttrs);
			mLog.info("Attributes: " + (lTmpRes == null ? "none" : lTmpRes.toString()));

			Map<String, String> lDLs = aAD.getUserDLs(lTestUserId);
			mLog.info("DLs for '" + lDN_User2 + "':");
			mLog.info("Group-Names: " + lDLs.keySet().toString());

			boolean lUser0InTestTL1 = aAD.isUserInDL(lTestUserId, mTestDL1, lDLs);
			boolean lUser0InOtherDL = aAD.isUserInDL(lTestUserId, "ArbitraryDLNameForTest", lDLs);
			mLog.info("In '" + mTestDL1 + "'?: " + lUser0InTestTL1);
			mLog.info("In other DL?: " + lUser0InOtherDL);

			lDNs = aAD.getUserDLs(mTestUserNames[0]);
			mLog.info("DLs for " + mTestUserNames[0] + ":");
			mLog.info("Group-Names: " + lDNs.keySet().toString());

			lDNs = aAD.getUserDLs(mTestUserNames[1]);
			mLog.info("DLs for " + mTestUserNames[1] + ":");
			mLog.info("Group-Names: " + lDNs.keySet().toString());

			lDNs = aAD.getUserDLs(mTestUserNames[2]);
			mLog.info("DLs for " + mTestUserNames[2] + ":");
			mLog.info(lDNs.keySet().toString());

			lDNs = aAD.getUserDLs(mTestUserNames[3]);
			mLog.info("DLs for " + mTestUserNames[3] + ":");
			mLog.info(lDNs.keySet().toString());

			mLog.info("List DNs from " + mTestDL0 + ":");
			List<String> dns = aAD.getDLMemberDNs(mTestDL0);
			if (dns != null) {
				for (String dn : dns) {
					mLog.info(dn);
				}
			}

			// mLog.info("Deleting all users from " + mTestDL + ":");
			// clearDL(mTestDL);
			mLog.info("List DNs from " + mTestDL0 + ":");
			dns = aAD.getDLMemberDNs(mTestDL0);
			if (dns != null) {
				for (String dn : dns) {
					mLog.info(dn);
				}
			}

			mLog.info("Adding '" + mTestUserNames[0] + "' and '" + mTestUserNames[1] + "' to '" + mTestDL0 + "'...");
			aAD.addDNToDL(lDNDL0, lDN_User0);
			aAD.addDNToDL(lDNDL1, lDN_User1);

			mLog.info("List DNs from " + mTestDL0 + ":");
			dns = aAD.getDLMemberDNs(mTestDL0);
			if (dns != null) {
				for (String dn : dns) {
					mLog.info(dn);
				}
			}
		} else {
			mLog.error("Authenticaton failed");
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// set up log4j logging
		// later this should be read from a shared log4j properties or xml file!
		Properties lProps = new Properties();
		lProps.setProperty("log4j.rootLogger", "INFO, console");
		lProps.setProperty("log4j.logger.org.apache.activemq.spring", "WARN");
		lProps.setProperty("log4j.logger.org.apache.activemq.web.handler", "WARN");
		lProps.setProperty("log4j.logger.org.springframework", "WARN");
		lProps.setProperty("log4j.logger.org.apache.xbean", "WARN");
		lProps.setProperty("log4j.logger.org.apache.camel", "INFO");
		lProps.setProperty("log4j.logger.org.eclipse.jetty", "WARN");
		lProps.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
		lProps.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
		lProps.setProperty("log4j.appender.console.layout.ConversionPattern",
				// "%p: %m%n"
				"%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p - %C{1}: %m%n"
		);
		// set here the jWebSocket log level:
		lProps.setProperty("log4j.logger.org.jwebsocket", "DEBUG");
		lProps.setProperty("log4j.appender.console.threshold", "DEBUG");
		PropertyConfigurator.configure(lProps);

		init();
		lADTools = new ADTools(
				LDAP_URL, // URL to AD Server
				BASE_DN_GROUPS, // DN to start search for groups
				BASE_DN_USERS // DN to start search for users
		);
		// try to login a user with his given credentials
		String lMessage = lADTools.authenticate(USERNAME, PASSWORD);
		if (null == lMessage) {
			mLog.info(USERNAME + " successfull logged in.");
		} else {
			mLog.error(USERNAME + " could not be authenticated, reason: " + lMessage + ".");
		}

		lADTools.login(USERNAME, PASSWORD);

		demo(lADTools);

		lADTools.logout();
		/*
		 String[] lAttrs = {"distinguishedName", "name", "displayName", "mail", "mailNickname"};
		 Map<String, List<String>> lTmpRes = lAD.getUserAttrs("predrags", lAttrs);
		 mLog.info("Attributes: " + (lTmpRes == null ? "none" : lTmpRes.toString()));
		 */
		if (true) {
			return;
		}

//		FastList<LdapUser> lADUsers = lAD.getAllUsersList();
//		for (FastList.Node<LdapUser> lNodeADUsers = lADUsers.head(), end = lADUsers.tail(); (lNodeADUsers = lNodeADUsers.getNext()) != end;) {
//			LdapUser lADUser = lNodeADUsers.getValue();
//			if (lADUser.getGUID() == null || lADUser.getGUID().length() < 30) {
//				mLog.info(lADUser.getLoginName() + " [" + lADUser.getGUID() + "]");
//			}
//		}
		mLog.info(mTestUserNames[0] + ": " + lADTools.getDNfromLoginname(mTestUserNames[0]));
		mLog.info(mTestDL1 + " from DL: " + lADTools.getDNfromDL(mTestDL1));
		ADDistributionList lDL = lADTools.getDLfromName(mTestDL1);
		mLog.info(mTestDL1 + " from name: " + lDL.toString());
		lDL = lADTools.getDLfromGUID(lDL.getGUID());
		mLog.info(mTestDL1 + " from GUID: " + lDL.toString());

		mLog.info("----------------------");

		int lCnt = 0;
		FastList<ADUser> lUsers = lADTools.getAllUsersList();
		ADUser lADUser;
		for (FastList.Node<ADUser> lCurUser = lUsers.head(), lLastUser = lUsers.tail(); (lCurUser = lCurUser.getNext()) != lLastUser;) {
			lCnt++;
			if (lCnt > 10) {
				break;
			}
			lADUser = lCurUser.getValue();
			mLog.info(lADUser.toString());
			mLog.info("-from GUID: " + lADTools.getUserDNfromGUID(lADUser.getGUID()));
		}
		mLog.info("Total users found: " + lUsers.size());

//		FastList<String> list = lAD.getAllUserNamesList();
//		for (FastList.Node<String> n = list.head(), end = list.tail(); (n = n.getNext()) != end;) {
//			mLog.info(n.getValue());
//		}
//		mLog.info(list.size());
		lADTools.logout();
	}
}
