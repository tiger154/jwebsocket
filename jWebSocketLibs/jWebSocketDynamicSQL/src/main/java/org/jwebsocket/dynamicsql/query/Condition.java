//	---------------------------------------------------------------------------
//	jWebSocket - ClassPathUpdater (Community Edition, CE)
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
package org.jwebsocket.dynamicsql.query;

import java.util.Map;
import org.jwebsocket.dynamicsql.api.ICondition;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class Condition implements ICondition {

	private Map<String, Object> mAttrs;

	/**
	 * Constructor.
	 *
	 * @param aAttrs
	 */
	public Condition(Map<String, Object> aAttrs) {
		this.mAttrs = aAttrs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getCondition() {
		return mAttrs;
	}
}
