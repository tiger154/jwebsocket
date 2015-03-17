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
