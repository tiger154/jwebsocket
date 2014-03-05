// ---------------------------------------------------------------------------
// jWebSocket - LoggingConfig (Community Edition, CE)
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

/**
 * Configuration for logging User: aschulze
 */
public class LoggingConfig implements Config {

	private final Integer mReloadDelay;
	private final Integer mMaxLogTokenLength;

	/**
	 * Constructor
	 *
	 * @param aReloadDelay
	 * @param aMaxLogTokenLength
	 */
	public LoggingConfig(Integer aReloadDelay, Integer aMaxLogTokenLength) {
		mReloadDelay = aReloadDelay;
		mMaxLogTokenLength = aMaxLogTokenLength;
	}

	/**
	 *
	 * @return
	 */
	public Integer getReloadDelay() {
		return mReloadDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
	}

	/**
	 * @return the mMaxLogTokenLength
	 */
	public Integer getMaxLogTokenLength() {
		return mMaxLogTokenLength;
	}
}
