//	---------------------------------------------------------------------------
//	jWebSocket - Common Configuration Constants (Community Edition, CE)
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
package org.jwebsocket.config;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.kit.WebSocketEncoding;

/**
 *
 * @author Alexander Schulze
 * @author Rolando Betancourt Toucet
 */
public class JWebSocketCommonConstants {

	/**
	 * jWebSocket copyright string - NEEDS TO BE KEPT due to Apache License,
	 * Version 2.0! Please ask for conditions of a commercial license on demand.
	 */
	public static final String COPYRIGHT_CE = "(C) Copyright 2010-2014 Innotrade GmbH (jWebSocket.org), Germany (NRW), Herzogenrath";
	/**
	 * jWebSocket copyright string - NEEDS TO BE KEPT!
	 */
	public static final String COPYRIGHT_EE = COPYRIGHT_CE;
	;
	/**
	 * jWebSocket license string - NEEDS TO BE KEPT due to Apache License,
	 * Version 2.0! Please ask for conditions of a commercial license on demand.
	 */
	public static final String LICENSE_CE = "Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)";
	/**
	 * jWebSocket enterprise edition license string - NEEDS TO BE KEPT.
	 */
	public static final String LICENSE_EE = "Licensed under jWebSocket Enterprise License, Version 1.0 (http://jwebsocket.org/license-1.0/)";
	/**
	 * jWebSocket CE vendor string - NEEDS TO BE KEPT due to Apache License,
	 * Version 2.0! Please ask for conditions of a commercial license on demand.
	 */
	public static final String VENDOR_CE = "jWebSocket.org";
	/**
	 * jWebSocket EE vendor string - NEEDS TO BE KEPT!
	 */
	public static final String VENDOR_EE = "jWebSocket.com";
	/**
	 * jWebSocket sub protocol prefix
	 */
	public final static String WS_SUBPROT_PREFIX = "org.jwebsocket";
	/**
	 * jWebSocket JSON sub protocol
	 */
	public final static String WS_SUBPROT_JSON = WS_SUBPROT_PREFIX + ".json";
	/**
	 * jWebSocket CSV sub protocol
	 */
	public final static String WS_SUBPROT_CSV = WS_SUBPROT_PREFIX + ".csv";
	/**
	 * jWebSocket XML sub protocol
	 */
	public final static String WS_SUBPROT_XML = WS_SUBPROT_PREFIX + ".xml";
	/**
	 * jWebSocket custom specific text sub protocol
	 */
	public final static String WS_SUBPROT_TEXT = WS_SUBPROT_PREFIX + ".text";
	/**
	 * jWebSocket custom specific binary sub protocol
	 */
	public final static String WS_SUBPROT_BINARY = WS_SUBPROT_PREFIX + ".binary";
	/**
	 * Default protocol
	 */
	public static String WS_SUBPROT_DEFAULT = WS_SUBPROT_JSON;
	/**
	 * JSON sub protocol format
	 */
	public final static String WS_FORMAT_JSON = "json";
	/**
	 * CSV sub protocol format
	 */
	public final static String WS_FORMAT_CSV = "csv";
	/**
	 * XML sub protocol format
	 */
	public final static String WS_FORMAT_XML = "xml";
	/**
	 * Binary sub protocol format
	 */
	public final static String WS_FORMAT_BINARY = "binary";
	/**
	 * Custom specific sub protocol format
	 */
	public final static String WS_FORMAT_TEXT = "text";
	/**
	 * Default sub protocol format
	 */
	public static String WS_FORMAT_DEFAULT = WS_FORMAT_JSON;
	/**
	 * WebSocket protocol hybi draft 10
	 * (http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76)
	 */
	public final static String WS_HIXIE_DRAFT_76 = "76";
	/**
	 * WebSocket protocol hybi draft 03
	 * (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-02)
	 */
	public final static String WS_HYBI_DRAFT_02 = "2";
	/**
	 * WebSocket protocol hybi draft 03
	 * (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-03)
	 */
	public final static String WS_HYBI_DRAFT_03 = "3";
	/**
	 * WebSocket protocol hybi draft 07
	 * (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-07)
	 */
	public final static String WS_HYBI_DRAFT_07 = "7";
	/**
	 * WebSocket protocol hybi draft 08
	 * (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-08)
	 */
	public final static String WS_HYBI_DRAFT_08 = "8";
	/**
	 * WebSocket protocol hybi draft 10
	 * (http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10)
	 */
	public final static String WS_HYBI_DRAFT_10 = "10";
	/**
	 * WebSocket earliest supported hixie version
	 */
	public final static int WS_EARLIEST_SUPPORTED_HIXIE_VERSION = 75;
	/**
	 * WebSocket latest supported hixie version
	 */
	public final static int WS_LATEST_SUPPORTED_HIXIE_VERSION = 76;
	/**
	 * WebSocket latest supported hybi version
	 */
	public final static int WS_LATEST_SUPPORTED_HYBI_VERSION = 8;
	/**
	 * WebSocket earliest supported hixie draft
	 */
	public final static String WS_EARLIEST_SUPPORTED_HIXIE_DRAFT = "75";
	/**
	 * WebSocket latest supported hixie draft
	 */
	public final static String WS_LATEST_SUPPORTED_HIXIE_DRAFT = "76";
	/**
	 * WebSocket latest supported hybi draft
	 */
	public final static String WS_LATEST_SUPPORTED_HYBI_DRAFT = "10";
	/**
	 * WebSocket supported hixie versions
	 */
	public final static List<Integer> WS_SUPPORTED_HIXIE_VERSIONS = new FastList<Integer>();
	/**
	 * WebSocket supported hixie drafts
	 */
	public final static List<String> WS_SUPPORTED_HIXIE_DRAFTS = new FastList<String>();
	/**
	 * WebSocket supported hybi versions
	 */
	public final static List<Integer> WS_SUPPORTED_HYBI_VERSIONS = new FastList<Integer>();
	/**
	 * WebSocket supported hybi drafts
	 */
	public final static List<String> WS_SUPPORTED_HYBI_DRAFTS = new FastList<String>();
	/**
	 * WebSocket default protocol version
	 */
	public final static int WS_VERSION_DEFAULT = 13;
	/**
	 * WebSocket default protocol version
	 */
	public final static int WS_DRAFT_DEFAULT = 10;
	/**
	 * Use text format as default encoding for WebSocket Packets if not
	 * explicitly specified
	 */
	public final static WebSocketEncoding WS_ENCODING_DEFAULT = WebSocketEncoding.TEXT;
	/**
	 * Separator between the path and the argument list in the URL.
	 */
	public static String PATHARG_SEPARATOR = ";";
	/**
	 * Separator between the various URL arguments.
	 */
	public static String ARGARG_SEPARATOR = ",";
	/**
	 * Separator between the key and the value of each URL argument.
	 */
	public static String KEYVAL_SEPARATOR = "=";
	/**
	 * Minimum allow outgoing TCP Socket port.
	 */
	public static int MIN_IN_PORT = 1024;
	/**
	 * Maximum allow outgoing TCP Socket port.
	 */
	public static int MAX_IN_PORT = 65535;
	/**
	 * the default maximum frame size if not configured
	 */
	public static final int DEFAULT_MAX_FRAME_SIZE = 16384;
	/**
	 * Default socket port for jWebSocket clients.
	 */
	public static int DEFAULT_PORT = 8787;
	/**
	 * Default socket port for jWebSocket clients.
	 */
	public static int DEFAULT_SSLPORT = 9797;
	/**
	 * Default context on app servers and servlet containers
	 */
	public static final String JWEBSOCKET_DEF_CONTEXT = "/jWebSocket";
	/**
	 * Default servlet on app servers and servlet containers
	 */
	public static final String JWEBSOCKET_DEF_SERVLET = "/jWebSocket";
	/**
	 * Default Session Timeout for client connections (120000ms = 2min)
	 */
	public static int DEFAULT_TIMEOUT = 120000;
	/**
	 * private scope, only authenticated user can read and write his personal
	 * items
	 */
	public static final String SCOPE_PRIVATE = "private";
	/**
	 * public scope, everybody can read and write items from this scope
	 */
	public static final String SCOPE_PUBLIC = "public";
	/**
	 * jWebSocket cookie name for the session identifier used when running in
	 * standalone mode.
	 */
	public static final String SESSIONID_COOKIE_NAME = "JWSSESSIONID";
	/**
	 * jWebSocket encoding mechanism constant
	 */
	public static final String ENCODING_FORMATS_VAR_KEY = "encodingFormats";

	static {
		// hixie support (versions)
		WS_SUPPORTED_HIXIE_VERSIONS.add(75);
		WS_SUPPORTED_HIXIE_VERSIONS.add(76);

		// hixie support (drafts)
		WS_SUPPORTED_HIXIE_DRAFTS.add("75");
		WS_SUPPORTED_HIXIE_DRAFTS.add("76");

		// hybi support (versions)
		WS_SUPPORTED_HYBI_VERSIONS.add(6);
		WS_SUPPORTED_HYBI_VERSIONS.add(7);
		WS_SUPPORTED_HYBI_VERSIONS.add(8);
		WS_SUPPORTED_HYBI_VERSIONS.add(9);
		WS_SUPPORTED_HYBI_VERSIONS.add(10);
		WS_SUPPORTED_HYBI_VERSIONS.add(11);
		WS_SUPPORTED_HYBI_VERSIONS.add(12);
		WS_SUPPORTED_HYBI_VERSIONS.add(13);

		// hybi support (drafts)
		WS_SUPPORTED_HYBI_DRAFTS.add("6");
		WS_SUPPORTED_HYBI_DRAFTS.add("7");
		WS_SUPPORTED_HYBI_DRAFTS.add("8");
		WS_SUPPORTED_HYBI_DRAFTS.add("9");
		WS_SUPPORTED_HYBI_DRAFTS.add("10");
		WS_SUPPORTED_HYBI_DRAFTS.add("11");
		WS_SUPPORTED_HYBI_DRAFTS.add("12");
		WS_SUPPORTED_HYBI_DRAFTS.add("13");
		WS_SUPPORTED_HYBI_DRAFTS.add("14");
		WS_SUPPORTED_HYBI_DRAFTS.add("15");
		WS_SUPPORTED_HYBI_DRAFTS.add("16");
		WS_SUPPORTED_HYBI_DRAFTS.add("17");
	}
}
