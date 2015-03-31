//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Pattern Layout (Community Edition, CE)
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
package org.jwebsocket.plugins.logging;

import org.apache.log4j.PatternLayout;

/**
 *
 * @author Victor Antonio Barzana Crespo
 */
public final class JWSJDBCPatternLayout extends PatternLayout {

	public JWSJDBCPatternLayout() {
	}

	public JWSJDBCPatternLayout(String aPattern) {
		super(aPattern);
	}

	/**
	 * Returns PatternParser used to parse the conversion string. Subclasses may
	 * override this to return a subclass of PatternParser which recognize
	 * custom conversion characters.
	 *
	 * @param aPattern The pattern
	 * @return JWSJDBCPatternParser The pattern parser
	 */
	@Override
	protected JWSJDBCPatternParser createPatternParser(String aPattern) {
		return new JWSJDBCPatternParser(aPattern);
	}

	public String format() {
		return super.format(null);
	}
}
