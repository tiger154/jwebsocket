// ---------------------------------------------------------------------------
// jWebSocket - RoleConfig (Community Edition, CE)
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

import java.util.Collections;
import java.util.List;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Immutable class for <tt>role</tt> configuration
 *
 * @author puran
 * @version $Id: RoleConfig.java 596 2010-06-22 17:09:54Z fivefeetfurther $
 *
 */
public final class RoleConfig implements Config {

	private final String id;
	private final String description;
	private final List<String> rights;

	/**
	 * Default constructor for role config
	 *
	 * @param id the role id
	 * @param description the role description
	 * @param rights the list of rights for that role
	 */
	public RoleConfig(String id, String description, List<String> rights) {
		this.id = id;
		this.description = description;
		this.rights = rights;
		validate();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the rights
	 */
	public List<String> getRights() {
		return Collections.unmodifiableList(rights);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate() {
		if ((id != null && id.length() > 0)
				&& (description != null && description.length() > 0)) {
			return;
		}
		throw new WebSocketRuntimeException(
				"Missing one of the role configuration, please check your configuration file");
	}
}
