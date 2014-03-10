// ---------------------------------------------------------------------------
// jWebSocket - LibraryConfig (Community Edition, CE)
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
 * immutable class that represents the <tt>library</tt> configuration
 *
 * @author Alexander Schulze
 */
public final class LibraryConfig implements Config {

	private final String mId;
	private final String mURL;
	private final String mDescription;

	/**
	 * default constructor
	 *
	 * @param aId the library id
	 * @param aURL the library url
	 * @param aDescription the description of the library
	 */
	public LibraryConfig(String aId, String aURL, String aDescription) {
		this.mId = aId;
		this.mURL = aURL;
		this.mDescription = aDescription;
		// validate libary config
		validate();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @return the url
	 */
	public String getURL() {
		return mURL;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((mId != null && mId.length() > 0)
				&& (mURL != null && mURL.length() > 0)
				&& (mDescription != null && mDescription.length() > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the libary configuration, "
				+ "please check your configuration file");
	}
}
