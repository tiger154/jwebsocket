//	---------------------------------------------------------------------------
//	jWebSocket OAuth demo for Java (Community Edition, CE)
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
package org.jwebsocket.sso;

/**
 * @author Alexander Schulze
 */
public class App {

	public static void main(String[] args) {

		OAuth lOAuth = new OAuth();

		lOAuth.setOAuthHost("https://aschulze-dt.nvidia.com");
		// lOAuth.setOAuthHost("https://hqdvmbl702:8443");

		lOAuth.setOAuthAppId("alsiusdev");
		lOAuth.setOAuthAppSecret("ffdf1D56-5825-49e5-Afb9-335393a94606]");

		// System.out.println("Test https call: " + lOAuth.testCall("https://hqdvmbl702:8443/get-smsession"));
		// System.out.println("Test https call: " + lOAuth.testCall("https://www.google.com"));
		System.out.println("Getting Session Cookie: " + lOAuth.getSSOSession("aschulze", "TryAgain#2014", 5000));
		System.out.println("Authenticate Session: " + lOAuth.authSession(lOAuth.getSessionId(), 5000));
		String lAccessToken = lOAuth.getAccessToken();
		/*		
		 System.out.println("JSON Direct Authentication: " + lOAuth.authDirect("aschulze", "Div#2014"));
		 */
		System.out.println("JSON User from Access Token: " + lOAuth.getUser(lAccessToken));
		/*
		 System.out.println("JSON Refresh Access Token: " + lOAuth.refreshAccessToken());
		 System.out.println("Username from OAuth Object: " + lOAuth.getUsername());
		 */

	}
}
