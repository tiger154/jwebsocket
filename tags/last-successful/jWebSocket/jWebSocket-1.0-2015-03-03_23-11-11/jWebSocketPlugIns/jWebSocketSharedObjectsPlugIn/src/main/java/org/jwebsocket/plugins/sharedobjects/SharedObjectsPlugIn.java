// ---------------------------------------------------------------------------
// jWebSocket - SharedObjectsPlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath
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
package org.jwebsocket.plugins.sharedobjects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javolution.util.FastList;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONStringer;
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
 * Pending...
 *
 * @author Alexander Schulze
 */
public class SharedObjectsPlugIn extends TokenPlugIn {

	private static Logger log = Logging.getLogger();
	// if namespace is changed update client plug-in accordingly!
	private final String NS_SHARED_OBJECTS
			= JWebSocketServerConstants.NS_BASE + ".plugins.sharedObjs";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket SharedObjectsPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION
			= "jWebSocket SharedObjects PlugIn - Community Edition";
	private final SharedObjects sharedObjects = new SharedObjects();
	// if data types are changed update client plug-in accordingly!
	private final List<String> DATA_TYPES = new FastList<String>(Arrays.asList(
			new String[]{"number", "string", "boolean", "object",
				"set", "list", "map", "table"}));

	/**
	 *
	 */
	public SharedObjectsPlugIn() {
		this(null);
	}

	/**
	 *
	 * @param aConfiguration
	 */
	public SharedObjectsPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		// specify default name space
		this.setNamespace(NS_SHARED_OBJECTS);
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
		return NS_SHARED_OBJECTS;
	}

	private boolean isDataTypeValid(String aDataType, WebSocketConnector aConnector, Token aResponse) {
		boolean isValid = ((aDataType != null && DATA_TYPES.contains(aDataType)));
		if (!isValid) {
			aResponse.setInteger("code", -1);
			aResponse.setString("msg", "invalid datatype '" + aDataType + "'");
			getServer().sendToken(aConnector, aResponse);
		}
		return isValid;
	}

	private boolean doesContain(String aID, WebSocketConnector aConnector, Token aResponse) {
		boolean isValid = ((aID != null && sharedObjects.contains(aID)));
		if (!isValid) {
			aResponse.setInteger("code", -1);
			aResponse.setString("msg", "object '" + aID + "' not found");
			getServer().sendToken(aConnector, aResponse);
		}
		return isValid;
	}

	private boolean alreadyExists(String aID, WebSocketConnector aConnector, Token aResponse) {
		boolean isValid = ((aID != null && sharedObjects.contains(aID)));
		if (isValid) {
			aResponse.setInteger("code", -1);
			aResponse.setString("msg", "object '" + aID + "' already exists");
			getServer().sendToken(aConnector, aResponse);
		}
		return isValid;
	}

	private Object string2Object(String aDataType, String aValue) {
		Object lRes = null;

		// number
		if (aDataType.equals("number")) {
			try {
				lRes = Double.parseDouble(aValue);
			} catch (NumberFormatException ex) {
			}

			// string
		} else if (aDataType.equals("string")) {
			lRes = aValue;
		}

		return lRes;
	}

	private String object2String(String aDataType, Object aObject) {
		// all supported objects should provide the toString() method
		return aObject.toString();
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();
		String lID = aToken.getString("id");
		String lDataType = aToken.getString("datatype");
		String lValue = aToken.getString("value");

		if (lType != null && getNamespace().equals(lNS)) {

			Token lResponse = getServer().createResponse(aToken);

			// create
			if (lType.equals("create")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing 'create'...");
				}
				if (!isDataTypeValid(lDataType, aConnector, lResponse)) {
					return;
				}
				if (alreadyExists(lID, aConnector, lResponse)) {
					return;
				}
				sharedObjects.put(lID, string2Object(lDataType, lValue));

				Token lBCT = TokenFactory.createToken(lNS, "event");
				lBCT.setString("name", "created");
				lBCT.setString("id", lID);
				lBCT.setString("datatype", lDataType);
				lBCT.setString("value", lValue);
				getServer().broadcastToken(aConnector, lBCT);

				// destroy
			} else if (lType.equals("destroy")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing 'destroy'...");
				}
				if (!doesContain(lID, aConnector, lResponse)) {
					return;
				}
				sharedObjects.remove(lID);

				Token lBCT = TokenFactory.createToken(lNS, "event");
				lBCT.setString("name", "destroyed");
				lBCT.setString("id", lID);
				getServer().broadcastToken(aConnector, lBCT);

				// get
			} else if (lType.equals("get")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing 'get'...");
				}
				if (!doesContain(lID, aConnector, lResponse)) {
					return;
				}
				Object lObj = sharedObjects.get(lID);
				lResponse.setString("id", lID);
				lResponse.setString("result", lObj.toString());

				// put
			} else if (lType.equals("update")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing 'update'...");
				}
				if (!isDataTypeValid(lDataType, aConnector, lResponse)) {
					return;
				}
				sharedObjects.put(lID, string2Object(lDataType, lValue));
				Token lBCT = TokenFactory.createToken(lNS, "event");
				lBCT.setString("name", "updated");
				lBCT.setString("id", lID);
				lBCT.setString("datatype", lDataType);
				lBCT.setString("value", lValue);
				getServer().broadcastToken(aConnector, lBCT);

				// init
			} else if (lType.equals("init")) {
				if (log.isDebugEnabled()) {
					log.debug("Processing 'init'...");
				}
				Token lBCT = TokenFactory.createToken(lNS, "event");
				lBCT.setString("name", "init");

				String lData = null;
				try {
					JSONStringer jsonStringer = new JSONStringer();
					// start main object
					jsonStringer.object();
					// iterate through all items (fields) of the token
					Iterator<String> lIterator = sharedObjects.getKeys().iterator();
					while (lIterator.hasNext()) {
						String lKey = lIterator.next();
						Object lVal = sharedObjects.get(lKey);
						if (lVal instanceof Collection) {
							jsonStringer.key(lKey).array();
							for (Object item : (Collection) lVal) {
								jsonStringer.value(item);
							}
							jsonStringer.endArray();
						} else {
							jsonStringer.key(lKey).value(lVal);
						}
					}
					// end main object
					jsonStringer.endObject();
					lData = jsonStringer.toString();
				} catch (JSONException ex) {
					log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
				lBCT.setString("value", lData);
				getServer().sendToken(aConnector, lBCT);

			} else {
				log.warn("Invalid command " + lType + " received...");
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", "invalid type '" + lType + "'");
			}

			getServer().sendToken(aConnector, lResponse);
		}

	}
}
