/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
	private static final char MESSAGE = 'm';
//	private static final char TIMESTAMP = 'd';
//	private static final char LEVEL = 'p'; // (debug, info, warn, error, fatal)
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
			String lResult = "";
			try {
				if (null != aLE) {
					Map lInfo = null;
					Object lMsg = aLE.getMessage();
					Object lAux;
					//	Trying to get info from the message, this could have been 
					//	sent via a Token using the loggingPlugIn
					if (null != lMsg) {
						lInfo = getInfoMapFromMsg((String) lMsg);
					} else {
						lMsg = "";
					}
					if (null != lInfo) {
						lMsg = lInfo.get("message");
						lInfo.remove("message");
					}
//			System.out.println("[JDBC Appender]: "
//					+ aLE.getLevel().toString() + ": "
//					+ lMsg
//					+ (lInfo != null
//							? ", info: " + lInfo
//							: "")
//			);
					// TODO: Extract the data from the event
					switch (mType) {
						case MESSAGE:
							lResult = lMsg.toString();
							break;
//				case TIMESTAMP:
//					// TODO: check how to get the timestamp properly from the event
//					lResult = org.jwebsocket.util.DateHandler.getCurrentDate() + " " + org.jwebsocket.util.DateHandler.getCurrentTime();
//					break;
//				case LEVEL:
//					lResult = aLE.getLevel().toString();
//					break;
						case CLASS_NAME:
							lResult = aLE.getLocationInformation().getClassName();
							break;
						case METHOD_NAME:
							lResult = aLE.getLocationInformation().getMethodName();
							break;
						case LINE_NUMBER:
							lResult = aLE.getLocationInformation().getLineNumber();
							break;
						case FILENAME:
							lResult = aLE.getLocationInformation().getFileName();
							break;
						case LOGGER_NAME:
							lResult = aLE.getLoggerName();
							break;
						case THREAD_NAME:
							lResult = aLE.getThreadName();
							break;
						case STACK_TRACE:
							ThrowableInformation lThrowableInfo = aLE.getThrowableInformation();
							if (null != lThrowableInfo) {
								lResult = Arrays.toString(lThrowableInfo.getThrowableStrRep());
							}
							break;

						// The following data should be supported in the Info map:
						case MAP_USERNAME:
							if (null != lInfo) {
								lAux = lInfo.get("user");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_IP_NUMBER:
							if (null != lInfo) {
								lAux = lInfo.get("ip");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_HOSTNAME:
							if (null != lInfo) {
								lAux = lInfo.get("hostname");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_CONNECTOR_ID:
							if (null != lInfo) {
								lAux = lInfo.get("connector_id");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_PRODUCT:
							if (null != lInfo) {
								lAux = lInfo.get("product");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_MODULE:
							if (null != lInfo) {
								lAux = lInfo.get("module");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_CLASSIFICATION:
							if (null != lInfo) {
								lAux = lInfo.get("classification");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_VERSION:
							if (null != lInfo) {
								lAux = lInfo.get("version");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_ENVIRONMENT:
							if (null != lInfo) {
								lAux = lInfo.get("environment");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_ERROR_CODE:
							if (null != lInfo) {
								lAux = lInfo.get("code");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_SYSTEM:
							if (null != lInfo) {
								lAux = lInfo.get("client");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_SYSTEM_VERSION:
							if (null != lInfo) {
								lAux = lInfo.get("system_version");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_CONDITION:
							if (null != lInfo) {
								lAux = lInfo.get("condition");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_SOURCE:
							if (null != lInfo) {
								lAux = lInfo.get("source");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						case MAP_TARGET:
							if (null != lInfo) {
								lAux = lInfo.get("target");
								if (null != lAux) {
									lResult = lAux.toString();
								}
							}
							break;
						default:
							break;
					}
				}
			} catch (Exception lEx) {
				System.out.println(lEx.getLocalizedMessage());
			}
			return lResult.replace("'", "");
		}
	}
}
