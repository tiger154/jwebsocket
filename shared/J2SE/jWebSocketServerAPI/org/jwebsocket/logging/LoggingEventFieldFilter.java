//	---------------------------------------------------------------------------
//	jWebSocket - LoggingEventFieldFilter (Community Edition, CE)
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
package org.jwebsocket.logging;

import java.util.List;
import javolution.util.FastList;
import org.apache.commons.lang.StringUtils;

/**
 * @author Victor Antonio Barzana Crespo
 */
public class LoggingEventFieldFilter {

	private String mFieldName;
	private List<String> mBlackList;
	private List<String> mWhiteList;
	private static final String REGEX_PATTERN = "(.*)";
	private static final String RULES_SEPARATOR = ",";
	private boolean mBlackListWildcard;
	private boolean mWhiteListWildcard;

	public String getFieldName() {
		return mFieldName;
	}

	public void setFieldName(String aFieldName) {
		this.mFieldName = aFieldName;
	}

	public String getBlackList() {
		if (null != mBlackList) {
			return StringUtils.join(mBlackList, RULES_SEPARATOR);
		}
		return null;
	}

	public void setBlackList(String aBlackList) {
		if (null != aBlackList) {
			mBlackList = new FastList<String>();
			String[] lSplit = aBlackList.split(RULES_SEPARATOR);
			String lTrimmed;
			for (String lEntry : lSplit) {
				lTrimmed = lEntry.trim();
				if ("*".equals(lTrimmed)) {
					mBlackListWildcard = true;
				} else {
					mBlackList.add(lTrimmed.replace("*", REGEX_PATTERN));
				}
			}
		} else {
			mBlackList = null;
		}
	}

	public String getWhiteList() {
		if (null != mWhiteList) {
			return StringUtils.join(mWhiteList, RULES_SEPARATOR);
		}
		return null;
	}

	public void setWhiteList(String aWhiteList) {
		if (null != aWhiteList) {
			mWhiteList = new FastList<String>();
			String[] lSplit = aWhiteList.split(RULES_SEPARATOR);
			String lTrimmed;
			for (String lEntry : lSplit) {
				lTrimmed = lEntry.trim();
				if ("*".equals(lTrimmed)) {
					mWhiteListWildcard = true;
				} else {
					mWhiteList.add(lTrimmed.replace("*", REGEX_PATTERN));
				}
			}
		} else {
			mWhiteList = null;
		}
	}

	/**
	 * Checks whether a given string matches the current white list, if it
	 * appears in the blacklist and not in the whitelist then it will be
	 * considered as a failure and false will be returned, e.g. Following the
	 * given configuration provided by the user: mFieldName is not required
	 * here. aField = "org.jwebsocket.plugins.logging.LoggingPlugIn" mWhiteList
	 * = "org.jwebsocket.plugins.logging.*" This case returns true because the
	 * given field matches the whitelist.
	 *
	 * @param aField
	 * @return false if the given field is contained within the blacklist
	 */
	public boolean matchesFilter(String aField) {
		boolean lResult = true,
				lMatchesBL = false,
				lMatchesWL = false;

		if (null == aField) {
			aField = "";
		}
		// Check if matches BlackList
		// "startsWith*, *contains*, endsWith*
		if (null != mBlackList && mBlackList.size() > 0) {
			for (String lBLEntry : mBlackList) {
				if (aField.matches(lBLEntry)) {
					lMatchesBL = true;
					break;
				}
			}
		}
		// When the list is empty is considered that matches the whiteList
		if (null != mWhiteList && mWhiteList.size() > 0) {
			for (String lWLEntry : mWhiteList) {
				if (aField.matches(lWLEntry)) {
					lMatchesWL = true;
					break;
				}
			}
		} else {
			lMatchesWL = true;
		}
		if (lMatchesBL || mBlackListWildcard) {
			// If for example, the user provided a configuration blackList="*", 
			// whiteList="somethingAllowed*" we need to deny everything, but 
			// allow that which starts with somethingAllowed
			if (mBlackListWildcard) {
				lResult = lMatchesWL && !mWhiteListWildcard;
			} // If matches the whitelist with a wildcard e.g. whiteList="*", 
			// blackList="*notThisOne*" then we consider that everything is 
			// allowed, except something that contains "notThisOne"
			else if (mWhiteListWildcard) {
				lResult = false;
			}
		}

		return lResult;
	}
}
