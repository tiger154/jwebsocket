// ---------------------------------------------------------------------------
// jWebSocket - PropertyConfig (Community Edition, CE)
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
package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * immutable class that represents the <tt>property</tt> configuration
 *
 * @author Rolando Santamaria Maso
 */
public final class PropertyConfig implements Config {

	private String mName, mValue;

	/**
	 *
	 * @param mName
	 * @param mValue
	 */
	public PropertyConfig(String mName, String mValue) {
		this.mName = mName;
		this.mValue = mValue;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return mName;
	}

	/**
	 *
	 * @return
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if (null != mName && null != mValue) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the property configuration, please check your configuration file");
	}
}
