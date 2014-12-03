//	---------------------------------------------------------------------------
//	jWebSocket - GetHashCode (Community Edition, CE)
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
package org.jwebsocket.eventmodel.event.test;

import org.jwebsocket.eventmodel.annotation.ImportFromToken;
import org.jwebsocket.eventmodel.event.C2SEvent;

/**
 * Get the hash code for a giving "text" parameter
 *
 * @author Rolando Santamaria Maso
 */
public class GetHashCode extends C2SEvent {

	private String mText;

	/**
	 * @return The text to get the hash code
	 */
	public String getText() {
		return mText;
	}

	/**
	 * @param text The text to get the hash code to set
	 */
	@ImportFromToken
	public void setText(String text) {
		this.mText = text;
	}
}
