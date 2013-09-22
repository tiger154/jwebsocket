//	---------------------------------------------------------------------------
//	jWebSocket OAuth demo for Java (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
 * @author aschulze
 */
public class App {

	public static void main(String[] args) {
		OAuth lOAuth = new OAuth();
		// lOAuth.setBaseURL("https://localhost/as/token.oauth2");
		lOAuth.setBaseURL("https://hqdvpngpoc01.nvidia.com/as/token.oauth2");
		lOAuth.setClientSecret("2Federate");
		System.out.println("JSON Direct Authentication: " + lOAuth.authDirect("aschulze@nvidia.com", "Yami#2812"));
		System.out.println("JSON User from Access Token: " + lOAuth.getUser());
		System.out.println("JSON Refresh Access Token: " + lOAuth.refreshAccessToken());
		System.out.println("Username from OAuth Object: " + lOAuth.getUsername());
		
	}
}
