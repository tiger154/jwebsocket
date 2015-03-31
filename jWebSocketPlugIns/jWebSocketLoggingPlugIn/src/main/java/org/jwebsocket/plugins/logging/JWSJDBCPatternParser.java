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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

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
	private final char MESSAGE = 'm';
//	private  final char TIMESTAMP = 'd';
//	private  final char LEVEL = 'p'; // (debug, info, warn, error, fatal)
	private final char CLASS_NAME = 'c';// (from LocationInfo)
	private final char METHOD_NAME = 'M';// (from LocationInfo)
	private final char LINE_NUMBER = 'L';// (from LocationInfo)
	private final char FILENAME = 'f'; // (from LocationInfo)
	private final char LOGGER_NAME = 'g';
	private final char THREAD_NAME = 'T';
	private final char STACK_TRACE = 's'; // (if available from getThrowable information)

	// The following data should be supported in the Info map:
	private final char MAP_USERNAME = 'u';
	private final char MAP_IP_NUMBER = 'n';
	private final char MAP_HOSTNAME = 'h';
	private final char MAP_PRODUCT = 'P';
	private final char MAP_MODULE = 'D';
	private final char MAP_CLASSIFICATION = 'C';
	private final char MAP_VERSION = 'v';
	private final char MAP_ENVIRONMENT = 'e';
	private final char MAP_ERROR_CODE = 'E';
	private final char MAP_SYSTEM = 'S'; // (e.g. Browser, Java, OS)
	private final char MAP_SYSTEM_VERSION = 'V'; // VERSION
	private final char MAP_CONDITION = 'o'; // (success, failure, timeout)
	private final char MAP_SOURCE = 'U'; // (sender)
	private final char MAP_TARGET = 'G'; // (receiver)
	private final char MAP_CONNECTOR_ID = 'O';
	private LoggingEvent mLoggingEvent;
	private Map mInfo;
	private Object mMessage;

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
	 * the query
	 */
	private class JWSLog4jPatternConverter extends PatternConverter {

		private final char mType;

		public JWSLog4jPatternConverter(char aType) {
			super();
			mType = aType;
		}

		@SuppressWarnings("unchecked")
		public Map getInfoMapFromMsg(String aMsg) {
			try {
				if (null == aMsg) {
					return null;
				}
				int lIdx = aMsg.lastIndexOf(", info:");
				if (lIdx < 0) {
					return null;
				}
				String lJSON = aMsg.substring(lIdx + 8);
				Map lMap = new ObjectMapper().readValue(lJSON, HashMap.class);
				lMap.put("message", aMsg.substring(0, lIdx));
				return lMap;
			} catch (Exception lEx) {
				return null;
			}
		}

		@Override
		protected String convert(LoggingEvent aLE) {
			// Caching the logging event so this algorithm runs only once per logging event
			if (null != aLE && (null == mLoggingEvent
					|| mLoggingEvent.timeStamp != aLE.timeStamp)) {
				mLoggingEvent = aLE;
				mMessage = mLoggingEvent.getMessage();
				//	Trying to get info from the message, this could have been 
				//	sent via a Token using the loggingPlugIn
				if (null != mMessage) {
					mInfo = getInfoMapFromMsg((String) mMessage);
				} else {
					mMessage = "";
				}
				if (null != mInfo) {
					mMessage = mInfo.get("message");
					mInfo.remove("message");
				}
			}
			String lResult = "";
			try {
				if (null != mLoggingEvent) {
					// LoggingEvent related information
					switch (mType) {
						case MESSAGE:
							if (null != mMessage) {
								lResult = mMessage.toString();
							}
							break;
						case CLASS_NAME:
							lResult = mLoggingEvent.getLocationInformation().getClassName();
							break;
						case METHOD_NAME:
							lResult = mLoggingEvent.getLocationInformation().getMethodName();
							break;
						case LINE_NUMBER:
							lResult = mLoggingEvent.getLocationInformation().getLineNumber();
							break;
						case FILENAME:
							lResult = mLoggingEvent.getLocationInformation().getFileName();
							break;
						case LOGGER_NAME:
							lResult = mLoggingEvent.getLoggerName();
							break;
						case THREAD_NAME:
							lResult = mLoggingEvent.getThreadName();
							break;
						case STACK_TRACE:
							ThrowableInformation lThrowableInfo = mLoggingEvent.getThrowableInformation();
							if (null != lThrowableInfo) {
								lResult = Arrays.toString(lThrowableInfo.getThrowableStrRep());
							}
							break;
						default:
							break;
					}
					// info Map related data
					if (null != mInfo) {
						Object lAux;
						switch (mType) {
							// The following data should be supported in the Info map:
							case MAP_USERNAME:
								if (null != mInfo) {
									lAux = mInfo.get("user");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_IP_NUMBER:
								if (null != mInfo) {
									lAux = mInfo.get("ip");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_HOSTNAME:
								if (null != mInfo) {
									lAux = mInfo.get("hostname");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_CONNECTOR_ID:
								if (null != mInfo) {
									lAux = mInfo.get("connector_id");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_PRODUCT:
								if (null != mInfo) {
									lAux = mInfo.get("product");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_MODULE:
								if (null != mInfo) {
									lAux = mInfo.get("module");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_CLASSIFICATION:
								if (null != mInfo) {
									lAux = mInfo.get("classification");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_VERSION:
								if (null != mInfo) {
									lAux = mInfo.get("version");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_ENVIRONMENT:
								if (null != mInfo) {
									lAux = mInfo.get("environment");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_ERROR_CODE:
								if (null != mInfo) {
									lAux = mInfo.get("code");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_SYSTEM:
								if (null != mInfo) {
									lAux = mInfo.get("client");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_SYSTEM_VERSION:
								if (null != mInfo) {
									lAux = mInfo.get("system_version");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_CONDITION:
								if (null != mInfo) {
									lAux = mInfo.get("condition_value");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_SOURCE:
								if (null != mInfo) {
									lAux = mInfo.get("source");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							case MAP_TARGET:
								if (null != mInfo) {
									lAux = mInfo.get("target");
									if (null != lAux) {
										lResult = lAux.toString();
									}
								}
								break;
							default:
								break;
						}
					}
				}
			} catch (Exception lEx) {
				System.out.println(lEx.getLocalizedMessage());
			}
			return lResult.replace("'", "''").replace("\\", "\\\\");
		}
	}
}
