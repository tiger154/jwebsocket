//	---------------------------------------------------------------------------
//	jWebSocket - LoggingEventFields (Community Edition, CE)
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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * This is an Util class to extract information related to a LoggingEvent, it 
 * should be instantiated before using it, however, the LoggingEvent currently 
 * supported fields are provided as static, so they can be generically used.
 * 
 * @author Victor Antonio Barzana Crespo
 */
public class LoggingEventFields {

	private LoggingEvent mLoggingEvent;
	private Map mInfo;
	private Object mMessage;

	public static final String MESSAGE = "message",
			TIME_STAMP = "time_stamp",
			LEVEL = "level",
			CLASS_NAME = "class_name",
			METHOD_NAME = "method_name",
			LINE_NUMBER = "line_number",
			FILENAME = "filename",
			LOGGER_NAME = "logger_name",
			THREAD_NAME = "thread_name",
			STACK_TRACE = "stack_trace",
			USERNAME = "username",
			IP_NUMBER = "ip_number",
			HOSTNAME = "hostname",
			PRODUCT = "product",
			MODULE = "module",
			CLASSIFICATION = "classification",
			VERSION = "version",
			ENVIRONMENT = "environment",
			ERROR_CODE = "error_code",
			SYSTEM = "system",
			SYSTEM_VERSION = "system_version",
			CONDITION_VALUE = "condition_value",
			SOURCE = "source",
			TARGET = "target",
			CONNECTOR_ID = "connector_id";

	@SuppressWarnings("unchecked")
	private Map getInfoMapFromMsg(String aMsg) {
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
		} catch (IOException lEx) {
			return null;
		}
	}

	/**
	 * This method provides a generic way to extract the fields from a LoggingEvent
	 * @param aLE The LoggingEvent to extract information from
	 * @param aFieldName The name of the field we want to look up in the provided logging event
	 * @return The value of the field that we want to extract from the LoggingEvent
	 * @throws Exception 
	 */
	public String getFieldValueFromEvent(LoggingEvent aLE, String aFieldName) throws Exception {
		// Caching the logging event so this algorithm runs only once per LoggingEvent
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

		if (null != mLoggingEvent) {
			if (MESSAGE.equals(aFieldName)) {
				if (null != mMessage) {
					lResult = mMessage.toString();
				}
			} else if (TIME_STAMP.equals(aFieldName)) {
				lResult = String.valueOf(mLoggingEvent.timeStamp);
			} else if (LEVEL.equals(aFieldName)) {
				lResult = mLoggingEvent.getLevel().toString();
			} else if (CLASS_NAME.equals(aFieldName)) {
				lResult = mLoggingEvent.getLocationInformation().getClassName();
			} else if (METHOD_NAME.equals(aFieldName)) {
				lResult = mLoggingEvent.getLocationInformation().getMethodName();
			} else if (LINE_NUMBER.equals(aFieldName)) {
				lResult = mLoggingEvent.getLocationInformation().getLineNumber();
			} else if (FILENAME.equals(aFieldName)) {
				lResult = mLoggingEvent.getLocationInformation().getFileName();
			} else if (LOGGER_NAME.equals(aFieldName)) {
				lResult = mLoggingEvent.getLoggerName();
			} else if (THREAD_NAME.equals(aFieldName)) {
				lResult = mLoggingEvent.getThreadName();
			} else if (STACK_TRACE.equals(aFieldName)) {
				ThrowableInformation lThrowableInfo = mLoggingEvent.getThrowableInformation();
				if (null != lThrowableInfo) {
					lResult = Arrays.toString(lThrowableInfo.getThrowableStrRep());
				}
			}
			// MAP INFO related information
			if (null != mInfo) {
				Object lAux;
				if (USERNAME.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("user");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (IP_NUMBER.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("ip");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (HOSTNAME.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("hostname");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (PRODUCT.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("product");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (MODULE.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("module");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (CLASSIFICATION.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("classification");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (VERSION.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("version");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (ENVIRONMENT.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("environment");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (ERROR_CODE.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("code");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (SYSTEM.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("client");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (SYSTEM_VERSION.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("system_version");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (CONDITION_VALUE.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("condition_value");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (SOURCE.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("source");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (TARGET.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("target");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				} else if (CONNECTOR_ID.equals(aFieldName)) {
					if (null != mInfo) {
						lAux = mInfo.get("connector_id");
						if (null != lAux) {
							lResult = lAux.toString();
						}
					}
				}
			}
		}
		return lResult.replace("'", "''").replace("\\", "\\\\");
	}
}
