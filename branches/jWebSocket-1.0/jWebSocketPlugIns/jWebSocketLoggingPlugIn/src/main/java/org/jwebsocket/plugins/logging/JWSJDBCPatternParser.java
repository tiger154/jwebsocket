//	---------------------------------------------------------------------------
//	jWebSocket - JDBC Pattern Parser (Community Edition, CE)
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

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.jwebsocket.logging.Logging;

/**
 *
 * @author Victor Antonio Barzana Crespo
 */
public class JWSJDBCPatternParser extends PatternParser {
	/*
	 Note: used converters already
	 'c' = CategoryPatternConverter
	 'C' = ClassNamePatternConverter
	 'd' = DatePatternConverter
	 'F', 'l', 'L' = LocationPatternConverter
	 'm' = BasicPatternConverter MESSAGE_CONVERTER
	 'M' = METHOD LocationPatternConverter
	 'p'	= BasicPatternConverter LEVEL_CONVERTER)
	 'r' = BasicPatternConverter  RELATIVE_TIME_CONVERTER
	 't' = BasicPatternConverter(formattingInfo, THREAD_CONVERTER
	 'x' = BasicPatternConverter(formattingInfo, NDC_CONVERTER)
	 'X' = MDCPatternConverter
	 */

	// From the LoggerEvent we get the following data:
	private static final char MESSAGE = 'm';
//	private  final char TIMESTAMP = 'd';
//	private  final char LEVEL = 'p'; // (debug, info, warn, error, fatal)
	private static final char CLASS_NAME = 'c';// (from LocationInfo)
	private static final char METHOD_NAME = 'M';// (from LocationInfo)
	private static final char LINE_NUMBER = 'L';// (from LocationInfo)
	private static final char FILENAME = 'f'; // (from LocationInfo)
	private static final char LOGGER_NAME = 'g';
	private static final char THREAD_NAME = 'T';
	private static final char STACK_TRACE = 's'; // (if available from getThrowable information)

	// The following data should be supported in the Info map:
	private static final char MAP_USERNAME = 'u';
	private static final char MAP_IP_NUMBER = 'n';
	private static final char MAP_HOSTNAME = 'h';
	private static final char MAP_PRODUCT = 'P';
	private static final char MAP_MODULE = 'D';
	private static final char MAP_CLASSIFICATION = 'C';
	private static final char MAP_VERSION = 'v';
	private static final char MAP_ENVIRONMENT = 'e';
	private static final char MAP_ERROR_CODE = 'E';
	private static final char MAP_SYSTEM = 'S'; // (e.g. Browser, Java, OS)
	private static final char MAP_SYSTEM_VERSION = 'V'; // VERSION
	private static final char MAP_CONDITION = 'o'; // (success, failure, timeout)
	private static final char MAP_SOURCE = 'U'; // (sender)
	private static final char MAP_TARGET = 'G'; // (receiver)
	private static final char MAP_CONNECTOR_ID = 'O';
	private static final LoggingEventFields mLoggingEventFields = new LoggingEventFields();
	private static final Logger mLog = Logging.getLogger();

	public JWSJDBCPatternParser(String aPattern) {
		super(aPattern);
	}

	@Override
	protected void finalizeConverter(char aChar) {
		switch (aChar) {
			case MESSAGE:
//			case TIMESTAMP://Timestamp delegated from the parser itself
//			case LEVEL: Level captured automatically
			case CLASS_NAME:
			case METHOD_NAME:
			case LINE_NUMBER:
			case FILENAME:
			case LOGGER_NAME:
			case THREAD_NAME:
			case STACK_TRACE:
			// The following data should be supported in the Info map:
			case MAP_USERNAME:
			case MAP_IP_NUMBER:
			case MAP_HOSTNAME:
			case MAP_PRODUCT:
			case MAP_MODULE:
			case MAP_CLASSIFICATION:
			case MAP_VERSION:
			case MAP_ENVIRONMENT:
			case MAP_ERROR_CODE:
			case MAP_SYSTEM:
			case MAP_SYSTEM_VERSION:
			case MAP_CONDITION:
			case MAP_SOURCE:
			case MAP_TARGET:
			case MAP_CONNECTOR_ID:
				currentLiteral.setLength(0);
				addConverter(new JWSLog4jPatternConverter(aChar));
				break;
			default:
				super.finalizeConverter(aChar);
				break;
		}
	}

	/**
	 * This converter is internally used to generate the data required to fill
	 * the query with the proper values of each field
	 */
	private class JWSLog4jPatternConverter extends PatternConverter {

		private final char mType;

		public JWSLog4jPatternConverter(char aType) {
			super();
			mType = aType;
		}

		@Override
		protected String convert(LoggingEvent aLE) {
			String lResult = "";
			try {
				switch (mType) {
					case MESSAGE:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.MESSAGE);
						break;
					case CLASS_NAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.CLASS_NAME);
						break;
					case METHOD_NAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.METHOD_NAME);
						break;
					case LINE_NUMBER:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.LINE_NUMBER);
						break;
					case FILENAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.FILENAME);
						break;
					case LOGGER_NAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.LOGGER_NAME);
						break;
					case THREAD_NAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.THREAD_NAME);
						break;
					case STACK_TRACE:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.STACK_TRACE);
						break;
					// The following data should be supported in the Info map:
					case MAP_USERNAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.USERNAME);
						break;
					case MAP_IP_NUMBER:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.IP_NUMBER);
						break;
					case MAP_HOSTNAME:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.HOSTNAME);
						break;
					case MAP_CONNECTOR_ID:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.CONNECTOR_ID);
						break;
					case MAP_PRODUCT:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.PRODUCT);
						break;
					case MAP_MODULE:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.MODULE);
						break;
					case MAP_CLASSIFICATION:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.CLASSIFICATION);
						break;
					case MAP_VERSION:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.VERSION);
						break;
					case MAP_ENVIRONMENT:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.ENVIRONMENT);
						break;
					case MAP_ERROR_CODE:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.ERROR_CODE);
						break;
					case MAP_SYSTEM:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.SYSTEM);
						break;
					case MAP_SYSTEM_VERSION:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.SYSTEM_VERSION);
						break;
					case MAP_CONDITION:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.CONDITION_VALUE);
						break;
					case MAP_SOURCE:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.SOURCE);
						break;
					case MAP_TARGET:
						lResult = mLoggingEventFields.getFieldValueFromEvent(aLE,
								LoggingEventFields.TARGET);
						break;
					default:
						break;
				}
			} catch (Exception lEx) {
				mLog.error(lEx.getLocalizedMessage());
			}
			return lResult;
		}
	}
}
