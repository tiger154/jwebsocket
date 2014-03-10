//	---------------------------------------------------------------------------
//	jWebSocket - FileSystemPlugIn (Community Edition, CE)
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
package org.jwebsocket.client.plugins.filesystem;

import java.util.List;
import org.jwebsocket.api.WebSocketTokenClient;
import org.jwebsocket.client.plugins.BaseClientTokenPlugIn;
import org.jwebsocket.config.JWebSocketClientConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.token.WebSocketResponseTokenListener;
import org.jwebsocket.util.Tools;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class FileSystemPlugIn extends BaseClientTokenPlugIn {

	/**
	 *
	 */
	public static final String ALIAS_PRIVATE = "privateDir";
	/**
	 *
	 */
	public static final String ALIAS_PUBLIC = "publicDir";
	/**
	 *
	 */
	public static final String SCOPE_PRIVATE = "private";
	/**
	 *
	 */
	public static final String SCOPE_PUBLIC = "public";

	/**
	 *
	 * @param aClient
	 */
	public FileSystemPlugIn(WebSocketTokenClient aClient) {
		super(aClient, JWebSocketClientConstants.NS_FILESYSTEM);
	}

	/**
	 *
	 * @param aClient
	 * @param aNS
	 */
	public FileSystemPlugIn(WebSocketTokenClient aClient, String aNS) {
		super(aClient, aNS);
	}

	/**
	 * Retrieves the file list from a given alias.
	 *
	 * @param aAlias The alias value. <tt>Example: privateDir</tt>
	 * @param aFilemasks The filtering file masks. <tt>Example: ["txt"]</tt>
	 * @param aRecursive Recursive file listing flag.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void getFileList(String aAlias, List<String> aFilemasks, boolean aRecursive,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "getFilelist");
		lRequest.setString("alias", aAlias);
		lRequest.setBoolean("recursive", aRecursive);
		lRequest.setList("filemasks", aFilemasks);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Deletes a file in the user private scope.
	 *
	 * @param aFilename The filename value.
	 * @param aForce Force file delete flag.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void delete(String aFilename, boolean aForce, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "delete");
		lRequest.setString("filename", aFilename);
		lRequest.setBoolean("force", aForce);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Indicates if a custom file exists on a given alias.
	 *
	 * @param aAlias The alias value. <tt>Example: privateDir</tt>
	 * @param aFilename The filename value.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void exists(String aAlias, String aFilename, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "exists");
		lRequest.setString("filename", aFilename);
		lRequest.setString("alias", aAlias);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Loads a file from a given alias.
	 *
	 * @param aAlias The alias value. <tt>Example: privateDir</tt>
	 * @param aFilename The filename value.
	 * @param aDecode Indicates if the received file content should be "base64"
	 * decoded automatically.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void load(String aAlias, String aFilename, boolean aDecode, WebSocketResponseTokenListener aListener) throws WebSocketException {
		Token lRequest = TokenFactory.createToken(getNS(), "load");
		lRequest.setString("filename", aFilename);
		lRequest.setString("alias", aAlias);
		lRequest.setBoolean("decode", aDecode);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Saves a file in a given scope.
	 *
	 * @param aFilename The filename value.
	 * @param aData The file content. Is Base64 encoded automatically for
	 * transmission.
	 * @param aScope The scope value. <tt>Allowed values: [private, public]</tt>
	 * @param aNotify Indicates if the server should notify the file save to
	 * connected clients. The server notifies only if the scope value is public.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void save(String aFilename, byte[] aData, String aScope, boolean aNotify,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		aNotify = (aScope.equals(SCOPE_PUBLIC) && aNotify);
		String lData = Tools.base64Encode(aData);
		String lEncoding = "base64";

		Token lRequest = TokenFactory.createToken(getNS(), "save");
		lRequest.setString("filename", aFilename);
		lRequest.setString("scope", aScope);
		lRequest.setString("encoding", lEncoding);
		lRequest.setBoolean("notify", aNotify);
		lRequest.setString("data", lData);

		getTokenClient().sendToken(lRequest, aListener);
	}

	/**
	 * Saves a file in a given scope.
	 *
	 * @param aFilename The filename value.
	 * @param aData The file content. Is Base64 encoded automatically for
	 * transmission.
	 * @param aScope The scope value. <tt>Allowed values: [private, public]</tt>
	 * @param aNotify
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void save(String aFilename, String aData, String aScope, boolean aNotify,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		save(aFilename, aData.getBytes(), aScope, aNotify, aListener);
	}

	/**
	 * Sends a file to a targeted client.
	 *
	 * @param aTargetId The targeted client identifier.
	 * @param aFilename The filename value.
	 * @param aData The file content. Is Base64 encoded automatically for
	 * transmission.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void send(String aTargetId, String aFilename, String aData,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		send(aTargetId, aFilename, aData.getBytes(), aListener);
	}

	/**
	 * Sends a file to a targeted client.
	 *
	 * @param aTargetId The targeted client identifier.
	 * @param aFilename The filename value.
	 * @param aData The file content. Is Base64 encoded automatically for
	 * transmission.
	 * @param aListener The response listener.
	 * @throws WebSocketException
	 */
	public void send(String aTargetId, String aFilename, byte[] aData,
			WebSocketResponseTokenListener aListener) throws WebSocketException {
		String lData = Tools.base64Encode(aData);
		String lEncoding = "base64";

		Token lRequest = TokenFactory.createToken(getNS(), "save");
		lRequest.setString("filename", aFilename);
		lRequest.setString("encoding", lEncoding);
		lRequest.setString("data", lData);
		lRequest.setString("targetId", aTargetId);

		getTokenClient().sendToken(lRequest, aListener);
	}
}
