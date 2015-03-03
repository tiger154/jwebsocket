//	---------------------------------------------------------------------------
//	jWebSocket - ActiveDirectory Support (Community Edition, CE)
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

/**
 * Support for Microsoft Active Directory (AD) Features via LDAP
 *
 * @author Alexander Schulze
 */
public class ADTools {

	static final Logger mLog = Logger.getLogger(ADTools.class);
	private DirContext mCtx = null;
	private Map<String, String> mResult = null;
	// to prevent endless loopes with circular group membership references
	private Map<String, Integer> mAlreadyChecked = null;
	//
	private static String mLDAP_URL = "ldap://ldap.yourADhost.com:389";
	private static String mDL_BASE_DN = "DC=yourADhost,DC=com";
	private static String mUSER_BASE_DN = "DC=yourADhost,DC=com";
	private static String mUSERNAME = "username";
	private static String mPASSWORD = "password";
//	
//	 private String mLastUserDN = null;

	/**
	 *
	 */
	public ADTools() {
	}

	/**
	 *
	 * @param aLDAP_URL
	 */
	public ADTools(String aLDAP_URL) {
		mLDAP_URL = aLDAP_URL;
	}

	/**
	 *
	 * @param aLDAP_URL
	 * @param aDLBaseDN
	 * @param aUserBaseDN
	 */
	public ADTools(String aLDAP_URL, String aDLBaseDN, String aUserBaseDN) {
		mLDAP_URL = aLDAP_URL;
		mDL_BASE_DN = aDLBaseDN;
		mUSER_BASE_DN = aUserBaseDN;
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public DirContext login(String aUsername, String aPassword) {
		Properties lEnv = new Properties();
		lEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		lEnv.put(Context.SECURITY_PRINCIPAL, aUsername);
		lEnv.put(Context.SECURITY_CREDENTIALS, aPassword);
		lEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		lEnv.put(Context.PROVIDER_URL, mLDAP_URL);
		lEnv.put(Context.REFERRAL, "follow");
		lEnv.put("java.naming.ldap.attributes.binary", "objectguid");

		try {
			// try to open context
			mCtx = new InitialDirContext(lEnv);
			return mCtx;
		} catch (NamingException lEx) {
			return null;
		}
	}

	/**
	 *
	 * @param aUsername
	 * @param aPassword
	 * @return
	 * @throws NamingException
	 */
	public DirContext getDirContext(String aUsername, String aPassword) throws NamingException {
		Properties lEnv = new Properties();
		lEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		lEnv.put(Context.SECURITY_PRINCIPAL, aUsername);
		lEnv.put(Context.SECURITY_CREDENTIALS, aPassword);
		lEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		lEnv.put(Context.PROVIDER_URL, mLDAP_URL);
		lEnv.put(Context.REFERRAL, "follow");
		lEnv.put("java.naming.ldap.attributes.binary", "objectguid");

		// try to open context
		DirContext lCtx = new InitialDirContext(lEnv);
		return lCtx;
	}

	/**
	 *
	 *
	 * @param aUsername
	 * @param aPassword
	 * @return
	 */
	public String authenticate(String aUsername, String aPassword) {
		Properties lEnv = new Properties();
		lEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
		lEnv.put(Context.SECURITY_PRINCIPAL, aUsername);
		lEnv.put(Context.SECURITY_CREDENTIALS, aPassword);
		lEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		lEnv.put(Context.PROVIDER_URL, mLDAP_URL);
		lEnv.put(Context.REFERRAL, "follow");
		lEnv.put("java.naming.ldap.attributes.binary", "objectguid");

		try {
			// try to open context
			DirContext lCtx = new InitialDirContext(lEnv);
			// if no exception is fired all was ok
			return null;
		} catch (NamingException lEx) {
			return lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
		}
	}

	/**
	 *
	 */
	private Map<String, Map<String, List<String>>> query(String aBase, String aFilter, String[] aAttrs) {
		Map<String, Map<String, List<String>>> lResMap = new FastMap<String, Map<String, List<String>>>();
		try {
			SearchControls lSearchCtls = new SearchControls();
			lSearchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			lSearchCtls.setReturningAttributes(aAttrs);
			lSearchCtls.setReturningObjFlag(true);

			NamingEnumeration lResItems = mCtx.search(aBase, aFilter, lSearchCtls);
			while (lResItems.hasMore()) {
				SearchResult lRes = (SearchResult) lResItems.next();
				// DirContext entry = (DirContext)lRes.getObject();

				String lDN = null;
				Map<String, List<String>> lItems = new FastMap<String, List<String>>();

				Attributes lAttrs = lRes.getAttributes();
				NamingEnumeration lAttrEnum = lAttrs.getAll();
				while (lAttrEnum.hasMore()) {
					Attribute lAttr = (Attribute) lAttrEnum.next();
					for (int j = 0; j < lAttr.size(); j++) {
						String lKey = lAttr.getID();
						if (lKey.equals("distinguishedName")) {
							lDN = lAttr.get().toString();
						}
						String lValue = lAttr.get(j).toString();
						if (lKey.equals("objectGUID")) {
							byte[] lGUID = (byte[]) lAttr.get();
							lValue = decodeObjectGUID(lGUID).toUpperCase();
						}
						List<String> lValues = lItems.get(lKey);
						if (lValues == null) {
							lValues = new FastList<String>();
						}
						lValues.add(lValue);
						lItems.put(lKey, lValues);
					}
				}
				if (lDN != null) {
					lResMap.put(lDN, lItems);
				}
			}
		} catch (NamingException lEx) {
			mLog.error("Exception: " + lEx.getMessage());
		}
		return lResMap;
	}

	private static String getStackTrace(Throwable aThrowable) {
		final Writer lRes = new StringWriter();
		final PrintWriter lPW = new PrintWriter(lRes);
		aThrowable.printStackTrace(lPW);
		return lRes.toString();
	}

	/**
	 *
	 * @param aLoginname
	 * @param aAttrNames
	 * @return
	 */
	public Map<String, List<String>> getUserAttrs(String aLoginname, String[] aAttrNames) {
		String lFilter = "(&(ObjectClass=user)(sAMAccountName=" + aLoginname + "))";
		FastMap<String, Map<String, List<String>>> lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, aAttrNames);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> lRes = new FastMap<String, List<String>>();

		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			for (String aAttrName : aAttrNames) {
				FastList<String> lList = (FastList<String>) Items.get(aAttrName);
				if (lList != null) {
					lRes.put(aAttrName, lList);
				}
			}
		}
		return lRes;
	}

	/**
	 *
	 * @param aDN
	 * @param aAttrNames
	 * @return
	 */
	public Map<String, List<String>> getUserAttrsFromDN(String aDN, String[] aAttrNames) {
		String lFilter = "(&(ObjectClass=user)(distinguishedName=" + aDN + "))";
		FastMap<String, Map<String, List<String>>> lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, aAttrNames);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> lRes = new FastMap<String, List<String>>();

		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			for (String aAttrName : aAttrNames) {
				FastList<String> lList = (FastList<String>) Items.get(aAttrName);
				if (lList != null) {
					lRes.put(aAttrName, lList);
				}
			}
		}
		return lRes;
	}

	/**
	 * Returns the DN from a given login name (user's nickname) or null if the
	 * user could not be found.
	 *
	 * @param aLoginName login name to search for in the directory
	 * @return DN of the user or null if the user could not be found
	 */
	public String getDNfromLoginname(String aLoginName) {
		String lFilter = "(&(ObjectClass=user)(sAMAccountName=" + aLoginName + "))";
		String[] lAttrs = {"distinguishedName"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> Items = (Map<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("distinguishedName");
			if (lList != null) {
				return lList.getFirst();
			}
		}
		return null;
	}

	/**
	 * Returns the DN from a given user name or null if the user could not be
	 * found.
	 *
	 * @param aName username to search for in the directory
	 * @return DN of the user or null if the user could not be found
	 */
	public String getDNfromName(String aName) {
		String lFilter = "(&(ObjectClass=user)(name=" + aName + "))";
		String[] lAttrs = {"distinguishedName"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> Items = (Map<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("distinguishedName");
			if (lList != null) {
				return lList.getFirst();
			}
		}
		return null;
	}

	/**
	 * Returns the DN from a given Distribution List (DL) or null if the DL
	 * could not be found.
	 *
	 * @param aDL
	 * @return DN of the Distribution List (DL) or null if the DL could not be
	 * found
	 */
	public String getDNfromDL(String aDL) {
		String lFilter = "(&(|(ObjectClass=group)(ObjectClass=top))(|(sAMAccountName=" + aDL + ")(name=" + aDL + ")))";
		String[] lAttrs = {"distinguishedName"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mDL_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("distinguishedName");
			if (lList != null) {
				return lList.getFirst();
			}
		}
		return null;
	}

	/**
	 * Returns the Distribution List (DL) from the given name or null if the DL
	 * could not be found in the directory.
	 *
	 * @param aName the name of the DL to search for
	 * @return
	 */
	public ADDistributionList getDLfromName(String aName) {
		String lFilter = "(&(|(ObjectClass=group)(ObjectClass=top))(|(sAMAccountName=" + aName + ")(name=" + aName + ")))";
		String[] lAttrs = {"distinguishedName", "displayName", "mail", "objectGUID"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mDL_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		String lGUID = "", lName = "", lEmail = "";
		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("displayName");
			if (lList != null) {
				lName = lList.getFirst();
			}
			lList = (FastList<String>) Items.get("mail");
			if (lList != null) {
				lEmail = lList.getFirst();
			}
			lList = (FastList<String>) Items.get("objectGUID");
			if (lList != null) {
				lGUID = lList.getFirst();
			}
			return new ADDistributionList(lGUID, lName, lEmail);
		}
		return null;
	}

	/**
	 * Returns the Distribution List (DL) from the given GUID or null if the DL
	 * could not be found in the directory.
	 *
	 * @param aGUID the GUID of the DL to search for
	 * @return
	 */
	public ADDistributionList getDLfromGUID(String aGUID) {
		aGUID = encodeObjectGUID(aGUID);
		String lFilter = "(&(|(ObjectClass=group)(ObjectClass=top))(objectGUID=" + aGUID + "))";
		String[] lAttrs = {"distinguishedName", "displayName", "mail", "objectGUID"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mDL_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		String lGUID = "", lName = "", lEmail = "";
		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("displayName");
			if (lList != null) {
				lName = lList.getFirst();
			}
			lList = (FastList<String>) Items.get("mail");
			if (lList != null) {
				lEmail = lList.getFirst();
			}
			lList = (FastList<String>) Items.get("objectGUID");
			if (lList != null) {
				lGUID = lList.getFirst();
			}
			return new ADDistributionList(lGUID, lName, lEmail);
		}
		return null;
	}

	/**
	 * This method is <strong>IDENTICAL</strong> to getDLfromGUID except for the
	 * ObjectClass=<br />
	 * The reason for having two methods is only speed because the ObjectClass=
	 * filter specifies where in the LDAP tree to start searching.
	 *
	 * @param aGUID
	 * @return
	 */
	public String getUserDNfromGUID(String aGUID) {
		aGUID = encodeObjectGUID(aGUID);
		String lFilter = "(&(ObjectClass=user)(objectGUID=" + aGUID + "))";
		mLog.debug("getUserfromGUID '" + aGUID + "', filter: " + lFilter);
		String[] lQueryAttrs = {"distinguishedName"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lQueryAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("distinguishedName");
			if (lList != null) {
				return lList.getFirst();
			}
		}
		return null;
	}

	/**
	 * This method is <strong>IDENTICAL</strong> to getDLfromGUID except for the
	 * ObjectClass=<br />
	 * The reason for having two methods is only speed because the ObjectClass=
	 * filter specifies where in the LDAP tree to start searching.
	 *
	 * @param aEmployeeNumber
	 * @return
	 */
	public String getUserfromEmployeeNumber(String aEmployeeNumber) {
		String lFilter = "(&(ObjectClass=user)(employeeNumber=" + aEmployeeNumber + "))";
		mLog.debug("getUserfromEmployeeNumber::" + aEmployeeNumber + ":filter: " + lFilter);
		String[] lAttrs = {"distinguishedName"};
		FastMap<String, Map<String, List<String>>> lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lAttrs);
		Entry lEntry = lResMap.head().getNext();
		if (lEntry == null) {
			return null;
		}
		Map<String, List<String>> Items = (FastMap<String, List<String>>) lEntry.getValue();
		if (Items != null) {
			FastList<String> lList = (FastList<String>) Items.get("distinguishedName");
			if (lList != null) {
				return lList.getFirst();
			}
		}
		return null;
	}

	/**
	 * Adds the given Distinguished Name (DN) to the given Distribution List
	 * (DL), identified by their DN. This method is usually used to add a new
	 * user to the given DL.
	 *
	 * @param aDLDN the Distinguished Name (DN) from the Distribution List (DL)
	 * to add the new DN
	 * @param aDN the new DN to be added top the given Distribution List (DL)
	 * @return <tt>true</tt> if the DN could be added, otherwise <tt>false</tt>.
	 */
	public boolean addDNToDL(String aDLDN, String aDN) {
		try {
			Attributes lAttrs = new BasicAttributes(true);
			lAttrs.put("member", aDN);
			mCtx.modifyAttributes(aDLDN, DirContext.ADD_ATTRIBUTE, lAttrs);
			return true;
		} catch (NamingException lNEx) {
			mLog.error(lNEx.getClass().getSimpleName()
					+ " adding " + aDN + " to " + aDLDN + ": "
					+ lNEx.getMessage());
			return false;
		}
	}

	/**
	 * Removes the given Distinguished Name (DN) from the given Distribution
	 * List (DL), identified by their DN. This method is usually used to remove
	 * a user from the given DL.
	 *
	 * @param aDLDN the Distinguished Name (DN) from the Distribution List (DL)
	 * to add the new DN
	 * @param aDN the new DN to be added top the given Distribution List (DL)
	 * @return <tt>true</tt> if the DN could be removed, otherwise
	 * <tt>false</tt>.
	 */
	public boolean removeDNFromDL(String aDLDN, String aDN) {
		try {
			Attributes lAttrs = new BasicAttributes(true);
			lAttrs.put("member", aDN);
			mCtx.modifyAttributes(aDLDN, DirContext.REMOVE_ATTRIBUTE, lAttrs);
			return true;
		} catch (NamingException lNEx) {
			mLog.error(lNEx.getClass().getSimpleName()
					+ " removing " + aDN + " from " + aDLDN + ": "
					+ lNEx.getMessage());
			return false;
		}
	}

	private void checkGroups(List<String> aGroupDNs) {
		String lBase;
		String lFilter;
		Map<String, Map<String, List<String>>> lResMap;
		Map<String, List<String>> Items;
		String lGroupDN;
		List<String> lGroupDNs = new FastList<String>();

		String lGroupsQueryStr = "";

		for (String aGroupDN : aGroupDNs) {
			lGroupDN = (String) aGroupDN;
			Integer lCount = mAlreadyChecked.get(lGroupDN);
			if (lCount != null) {
				lCount++;
			} else {
				lGroupsQueryStr += "(distinguishedName=" + lGroupDN + ")";
				lCount = 1;
				mAlreadyChecked.put(lGroupDN, lCount);
			}
		}

		if (lGroupsQueryStr.length() > 0) {
			lGroupsQueryStr = "(|" + lGroupsQueryStr + ")";
			lBase = mDL_BASE_DN;
			lFilter = "(&(ObjectClass=group)" + lGroupsQueryStr + ")";
			String[] lGroupAttrs = {"distinguishedName", "sAMAccountName", "name", "memberOf"};
			lResMap = (Map<String, Map<String, List<String>>>) query(lBase, lFilter, lGroupAttrs);
			Iterator lIterator = lResMap.keySet().iterator();

			FastList<String> lGroups;
			while (lIterator.hasNext()) {
				Items = lResMap.get(lIterator.next());
				String lGroupName = ((FastList<String>) Items.get("name")).getFirst();
				lGroupDN = ((FastList<String>) Items.get("distinguishedName")).getFirst();
				lGroups = (FastList<String>) Items.get("memberOf");

				if (lGroups != null) {
					for (String lGroup : lGroups) {
						lGroupDN = (String) lGroup;
						Integer lExists = mAlreadyChecked.get(lGroupDN);
						if (lExists == null) {
							lGroupDNs.add(lGroupDN);
						}
					}
				}
				mResult.put(lGroupName, lGroupDN);
			}
			if (lGroupDNs.size() > 0) {
				checkGroups(lGroupDNs);
			}
		}
	}

	/**
	 *
	 * @param aLoginname
	 * @return
	 */
	public Map<String, String> getUserDLs(String aLoginname) {
		mResult = new FastMap<String, String>();
		mAlreadyChecked = new FastMap<String, Integer>();
//		 mLastUserDN = "-";

		try {
			FastMap<String, Map<String, List<String>>> lResMap;
			Entry lEntry;
			Map<String, List<String>> Items;

			String[] lUserAttrs = {"distinguishedName", "sAMAccountName", "memberOf"};
			String lFilter = "(&(ObjectClass=user)(sAMAccountName=" + aLoginname + "))";
			lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lUserAttrs); // lUserAttrs
			lEntry = lResMap.head().getNext();
			Items = (Map<String, List<String>>) lEntry.getValue();
			if (Items != null) {
//				FastList<String> lUserDNs = (FastList<String>) Items.get("distinguishedName");
//				if (lUserDNs != null) {
//					mLastUserDN = lUserDNs.getFirst();
//				}
				List<String> lGroupDNs = Items.get("memberOf");
				checkGroups(lGroupDNs);
				return mResult;
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName()
					+ " retreiving user DLs: "
					+ lEx.getMessage());
		}
		return null;
	}

	/**
	 *
	 * @param aLoginname
	 * @param aDL
	 * @param aDLs
	 * @return
	 */
	public boolean isUserInDL(String aLoginname, String aDL, Map<String, String> aDLs) {
		if (aDLs == null) {
			aDLs = getUserDLs(aLoginname);
		}
		if (aDLs == null) {
			return false;
		}
		return aDLs.get(aDL) != null;
	}

	/**
	 *
	 * @param aDL
	 * @return
	 */
	public List<String> getDLMemberDNs(String aDL) {
		String lFilter = "(&(ObjectClass=group)(name=" + aDL + "))";
		FastMap<String, Map<String, List<String>>> lResMap = (FastMap<String, Map<String, List<String>>>) query(mDL_BASE_DN, lFilter, null);

		for (FastMap.Entry<String, Map<String, List<String>>> e = lResMap.head(), end = lResMap.tail(); (e = e.getNext()) != end;) {
			Map<String, List<String>> Items = (FastMap<String, List<String>>) e.getValue();
			if (Items != null) {
				List<String> lList = Items.get("member");
				if (lList != null) {
					return lList;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param aDL
	 * @return
	 */
	public boolean clearDL(String aDL) {
		String lDLDN = getDNfromDL(aDL);
		List<String> lDNs = getDLMemberDNs(aDL);
		if (lDNs == null || lDNs.isEmpty()) {
			return false;
		}
		try {
			ModificationItem[] lModItems = new ModificationItem[lDNs.size()];
			int lIdx = 0;
			for (String lDN : lDNs) {
				BasicAttribute attr = new BasicAttribute("member", lDN);
				ModificationItem mod
						= new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
				lModItems[lIdx] = mod;
				lIdx++;
			}
			mCtx.modifyAttributes(lDLDN, lModItems);
			return true;
		} catch (NamingException lNEx) {
			mLog.error(lNEx.getClass().getSimpleName()
					+ " clearing DL: "
					+ lNEx.getMessage());
			return false;
		}
	}

	/**
	 *
	 * @return
	 */
	public FastMap<String, Map<String, List<String>>> getAllUsers() {
		String lFilter = "(&(ObjectClass=user)(sAMAccountName=a*))";
		String[] lAttrs = {"distinguishedName", "sAMAccountName"};
		FastMap<String, Map<String, List<String>>> lResMap
				= (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lAttrs);
		return lResMap;
	}

	/**
	 * @return list of all LDAP usernames
	 */
	public FastList<String> getAllUserNamesList() {
		String lFilter;
		String[] lQueryAttrs = {"distinguishedName", "sAMAccountName"};

		String lKey;
		FastMap<String, List<String>> lAttrs;
		FastList<String> lValues;
		FastList<String> lUserNames = new FastList<String>();
		FastMap<String, Map<String, List<String>>> lResMap;
		char lStartsWith = 'a';
		while (lStartsWith <= 'z') {
			lFilter = "(&(ObjectClass=user)(sAMAccountName=" + lStartsWith + "*))";
			lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lQueryAttrs);
			for (FastMap.Entry<String, Map<String, List<String>>> lResEntry = lResMap.head(), lResEnd = lResMap.tail(); (lResEntry = lResEntry.getNext()) != lResEnd;) {
				lAttrs = (FastMap<String, List<String>>) lResEntry.getValue();
				for (FastMap.Entry<String, List<String>> lAttrEntry = lAttrs.head(), lAttrEnd = lAttrs.tail(); (lAttrEntry = lAttrEntry.getNext()) != lAttrEnd;) {
					lKey = lAttrEntry.getKey();
					lValues = (FastList<String>) lAttrEntry.getValue();
					if (lKey.equalsIgnoreCase("sAMAccountName") && lValues.size() > 0) {
						lUserNames.add(lValues.get(0));
					}
				}
			}
			lStartsWith++;
		}
		return lUserNames;
	}

	/**
	 * @return list of all LDAP usernames
	 */
	public FastList<ADUser> getAllUsersList() {
		String lFilter;
		String[] lQueryAttrs = {"distinguishedName", "sAMAccountName", "mail",
			"givenName", "sn", "displayName", "objectGUID", "employeeNumber"};

		String lGUID, lEmployeeNo, lLoginName, lEmail, lFirstName,
				lLastName, lName, lDN, lKey;
		FastMap<String, List<String>> lAttrs;
		FastList<String> lValues;
		FastList<ADUser> lADUsers = new FastList<ADUser>();
		FastMap<String, Map<String, List<String>>> lResMap;
		char lStartsWith = 'a';
		while (lStartsWith <= 'z') {
			lFilter = "(&(ObjectClass=user)(sAMAccountName=" + lStartsWith + "*))";
			lResMap = (FastMap<String, Map<String, List<String>>>) query(mUSER_BASE_DN, lFilter, lQueryAttrs);
			for (FastMap.Entry<String, Map<String, List<String>>> lResEntry = lResMap.head(), lResLast = lResMap.tail(); (lResEntry = lResEntry.getNext()) != lResLast;) {
				lAttrs = (FastMap<String, List<String>>) lResEntry.getValue();

				lGUID = lEmployeeNo = lLoginName = lEmail = lFirstName = lLastName = lName = lDN = "";
				for (FastMap.Entry<String, List<String>> lAttrEntry = lAttrs.head(), lAttrEnd = lAttrs.tail(); (lAttrEntry = lAttrEntry.getNext()) != lAttrEnd;) {
					lKey = lAttrEntry.getKey();
					lValues = (FastList<String>) lAttrEntry.getValue();

					if (lKey.equalsIgnoreCase("sAMAccountName") && lValues.size() > 0) {
						lLoginName = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("mail") && lValues.size() > 0) {
						lEmail = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("givenName") && lValues.size() > 0) {
						lFirstName = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("sn") && lValues.size() > 0) {
						lLastName = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("objectGUID") && lValues.size() > 0) {
						lGUID = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("employeeNumber") && lValues.size() > 0) {
						lEmployeeNo = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("displayName") && lValues.size() > 0) {
						lName = lValues.get(0);
					} else if (lKey.equalsIgnoreCase("distinguishedName") && lValues.size() > 0) {
						lDN = lValues.get(0);
					}
				}
				lADUsers.add(new ADUser(lGUID, lLoginName, lEmail,
						lFirstName, lLastName, lName, lDN, lEmployeeNo));
			}
			lStartsWith++;
		}
		return lADUsers;
	}

	/**
	 *
	 */
	public void logout() {
		try {
			if (mCtx != null) {
				mCtx.close();
			}
		} catch (NamingException ex) {
			mLog.error(ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	/**
	 *
	 * @param objectGUID
	 * @return
	 */
	public String encodeObjectGUID(String objectGUID) {

		if (objectGUID != null && objectGUID.length() > 21 && !objectGUID.contains("-")) {
			objectGUID = objectGUID.substring(0, 8)
					+ "-" + objectGUID.substring(8, 12)
					+ "-" + objectGUID.substring(12, 16)
					+ "-" + objectGUID.substring(16, 20)
					+ "-" + objectGUID.substring(20).toUpperCase().trim();
		}

		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(objectGUID, "-");
		String[] s = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++) {
			s[i] = st.nextToken();
		}

		sb.append("\\").append(s[0].substring(6, 8));
		sb.append("\\").append(s[0].substring(4, 6));
		sb.append("\\").append(s[0].substring(2, 4));
		sb.append("\\").append(s[0].substring(0, 2));

		sb.append("\\").append(s[1].substring(2, 4));
		sb.append("\\").append(s[1].substring(0, 2));

		sb.append("\\").append(s[2].substring(2, 4));
		sb.append("\\").append(s[2].substring(0, 2));

		sb.append("\\").append(s[3].substring(0, 2));
		sb.append("\\").append(s[3].substring(2, 4));

		sb.append("\\").append(s[4].substring(0, 2));
		sb.append("\\").append(s[4].substring(2, 4));
		sb.append("\\").append(s[4].substring(4, 6));
		sb.append("\\").append(s[4].substring(6, 8));
		sb.append("\\").append(s[4].substring(8, 10));
		sb.append("\\").append(s[4].substring(10, 12));

		return sb.toString();
	}

	/**
	 *
	 * @param objectGUID
	 * @return
	 */
	public String decodeObjectGUID(byte[] objectGUID) {
		StringBuilder sb = new StringBuilder("");
		sb.append(addLeadingZero((int) objectGUID[3] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[2] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[1] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[0] & 0xFF));
		sb.append("-");
		sb.append(addLeadingZero((int) objectGUID[5] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[4] & 0xFF));
		sb.append("-");
		sb.append(addLeadingZero((int) objectGUID[7] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[6] & 0xFF));
		sb.append("-");
		sb.append(addLeadingZero((int) objectGUID[8] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[9] & 0xFF));
		sb.append("-");
		sb.append(addLeadingZero((int) objectGUID[10] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[11] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[12] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[13] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[14] & 0xFF));
		sb.append(addLeadingZero((int) objectGUID[15] & 0xFF));

		return sb.toString();
	}

	/**
	 *
	 * @param aInt
	 * @return
	 */
	public String addLeadingZero(int aInt) {
		return (aInt <= 0xF)
				? "0" + Integer.toHexString(aInt)
				: Integer.toHexString(aInt);
	}

	/**
	 * @return the DirectoryContext object used by this ActiveDirectoy tools
	 * instance.
	 */
	public DirContext getDirContext() {
		return mCtx;
	}
}
