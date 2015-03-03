// ---------------------------------------------------------------------------
// jWebSocket - RightConfig (Community Edition, CE)
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
 * immutable class that represents the <tt>right</tt> configuration
 *
 * @author puran
 * @version $Id: RightConfig.java 596 2010-06-22 17:09:54Z fivefeetfurther $
 *
 */
public final class RightConfig implements Config {

	private final String mId;
	private final String mNamespace;
	private final String mDescription;

	/**
	 * default constructor
	 *
	 * @param aId the right id
	 * @param aNamespace the right namespace
	 * @param aDescription the description
	 */
	public RightConfig(String aId, String aNamespace, String aDescription) {
		this.mId = aId;
		this.mNamespace = aNamespace;
		this.mDescription = aDescription;
		//validate right config
		validate();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return mNamespace;
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
				&& (mNamespace != null && mNamespace.length() > 0)
				&& (mDescription != null && mDescription.length() > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the right configuration, please check your configuration file");
	}
}
