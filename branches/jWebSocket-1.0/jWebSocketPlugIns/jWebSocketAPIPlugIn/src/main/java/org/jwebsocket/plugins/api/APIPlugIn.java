//	---------------------------------------------------------------------------
//  jWebSocket API Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.api;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Plug-in to export the server API
 *
 * @author Rolando Santamaria Maso
 * @author Alexander Schulze
 */
public class APIPlugIn extends TokenPlugIn {

	private static final Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	public static final String NS_API
			= JWebSocketServerConstants.NS_BASE + ".plugins.api";
	private final static String VERSION = "1.0.0";
	private final static String VENDOR = JWebSocketCommonConstants.VENDOR_CE;
	private final static String LABEL = "jWebSocket APIPlugIn";
	private final static String COPYRIGHT = JWebSocketCommonConstants.COPYRIGHT_CE;
	private final static String LICENSE = JWebSocketCommonConstants.LICENSE_CE;
	private final static String DESCRIPTION = "jWebSocket API Plug-in - Community Edition";
	private final static String GET_PLUG_IN_INFO = "getPlugInInfo";
	private final static String GET_SERVER_API = "getServerAPI";
	private final static String GET_PLUGIN_API = "getPlugInAPI";
	private final static String GET_PLUGIN_IDS = "getPlugInIds";
	private final static String SUPPORTS_TOKEN = "supportsToken";
	private final static String HAS_PLUGIN = "hasPlugin";
	private static final String NS_INTERFACE = NS_API;
	private BeanFactory mBeanFactory;

	/**
	 *
	 * @param configuration
	 * @throws Exception
	 */
	public APIPlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating API plug-in...");
		}

		// specify default name space for interface plugin
		this.setNamespace(NS_INTERFACE);

		try {
			// Creating the Spring Bean Factory
			mBeanFactory = getConfigBeanFactory(NS_INTERFACE);
		} catch (Exception lEx) {
			mLog.error(Logging.getSimpleExceptionMessage(lEx, "instantiating API plug-in"));
		}

		// give a success message to the administrator
		if (mLog.isInfoEnabled()) {
			mLog.info("API plug-in successfully instantiated.");
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
		return NS_API;
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (GET_PLUG_IN_INFO.equals(aToken.getType())) {
				getPlugInInfo(aConnector, aToken);
			} else if (GET_SERVER_API.equals(aToken.getType())) {
				getServerAPI(aConnector, aToken);
			} else if (GET_PLUGIN_API.equals(aToken.getType())) {
				getPlugInAPI(aConnector, aToken);
			} else if (GET_PLUGIN_IDS.equals(aToken.getType())) {
				getPlugInIds(aConnector, aToken);
			} else if (SUPPORTS_TOKEN.equals(aToken.getType())) {
				supportsToken(aConnector, aToken);
			} else if (HAS_PLUGIN.equals(aToken.getType())) {
				hasPlugIn(aConnector, aToken);
			}
		}
	}

	/**
	 * Export the server API
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getServerAPI(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		List<Token> lPlugIns = new FastList<Token>();
		Token lTempPlugIn;
		for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
			if (mBeanFactory.containsBean(lPlugIn.getId())) {
				lTempPlugIn = TokenFactory.createToken();
				PlugInDefinition pd = (PlugInDefinition) mBeanFactory.getBean(lPlugIn.getId());
				pd.writeToToken(lTempPlugIn);
				lPlugIns.add(lTempPlugIn);
			}
		}
		lResponse.setList("api", lPlugIns);

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Export the API for a plug-in giving a custom plug-in identifier
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getPlugInAPI(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		String lPlugInId = aToken.getString("plugin_id", null);
		if (null == lPlugInId) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'plugInId' parameter value!");

		} else if (!mBeanFactory.containsBean(lPlugInId)) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing '" + lPlugInId + "' plug-in definition!");
		} else {
			try {
				PlugInDefinition lPlugInDef = (PlugInDefinition) mBeanFactory.getBean(lPlugInId);
				lPlugInDef.writeToToken(lResponse);
			} catch (BeansException lEx) {
				lResponse.setInteger("code", -1);
				lResponse.setString("msg", lEx.getClass().getSimpleName() + ":" + lEx.getMessage());
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Export the plug-ins identifiers
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getPlugInIds(WebSocketConnector aConnector, Token aToken) {
		List<String> lIdentifiers = new FastList<String>();
		for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
			if (mBeanFactory.containsBean(lPlugIn.getId())) {
				lIdentifiers.add(lPlugIn.getId());
			}
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("identifiers", lIdentifiers);

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Export the plug-ins information record
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void getPlugInInfo(WebSocketConnector aConnector, Token aToken) {
		List<Map<String, Object>> lPlugInInfo = new FastList<Map<String, Object>>();
		Map<String, Object> lInfo;
		for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
			lInfo = new FastMap<String, Object>();
			lInfo.put("version", lPlugIn.getVersion());
			lInfo.put("copyright", lPlugIn.getCopyright());
			lInfo.put("copyright", lPlugIn.getCopyright());
			lInfo.put("description", lPlugIn.getDescription());
			lInfo.put("enabled", lPlugIn.getEnabled());
			lInfo.put("label", lPlugIn.getLabel());
			lInfo.put("license", lPlugIn.getLicense());
			lInfo.put("name", lPlugIn.getName());
			lInfo.put("namespace", lPlugIn.getNamespace());
			lInfo.put("id", lPlugIn.getId());
			lInfo.put("vendor", lPlugIn.getVendor());
			lPlugInInfo.add(lInfo);
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("pluginInfo", lPlugInInfo);

		// Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Giving a custom token type return <tt>TRUE</tt> if it is supported,
	 * <tt>FALSE</tt> otherwise
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void supportsToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		//Getting the plug-in identifier
		String lType = aToken.getString("token_type", null);
		if (null == lType) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'token_type' parameter value!");
		} else {
			lResponse.setBoolean("token_supported", Boolean.FALSE);

			for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
				if (mBeanFactory.containsBean(lPlugIn.getId())) {
					if (((PlugInDefinition) mBeanFactory.getBean(lPlugIn.getId())).supportsToken(lType)) {
						lResponse.setBoolean("token_supported", Boolean.TRUE);
						break;
					}
				}
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Giving a custom plug-in identifier return <tt>TRUE</tt> if it exists,
	 * <tt>FALSE</tt> otherwise
	 *
	 * @param aConnector
	 * @param aToken
	 */
	public void hasPlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		//Getting the plug-in identifier
		String lId = aToken.getString("plugin_id");
		if (null == lId) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'plugin_id' parameter value!");
		} else {
			if (null != getPlugInChain().getPlugIn(lId) && mBeanFactory.containsBean(lId)) {
				lResponse.setBoolean("has", Boolean.TRUE);
			} else {
				lResponse.setBoolean("has", Boolean.FALSE);
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}
}
