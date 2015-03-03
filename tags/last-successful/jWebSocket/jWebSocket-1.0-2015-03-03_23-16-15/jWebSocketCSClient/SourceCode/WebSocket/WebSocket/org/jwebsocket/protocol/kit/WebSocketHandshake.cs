/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketHandshake
 * Copyright (c) 2013 Rolando Betancourt Toucet
 * -------------------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
 * -------------------------------------------------------------------------------
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Cryptography;
using log4net;
using log4net.Config;
using System.Web;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.protocol.kit;

namespace WebSocket.org.jwebsocket.protocol.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Utility class for all the handshake.
    /// </summary>
    public class WebSocketHandshake
    {
        private Uri mUri = null;
        private string mHybiKey = null;
        private string mHybiKeyAccept = null;
        private string mOrigin = null;
        private string mProtocol = null;
        private int mVersion;
        private WebSocketCookieManager mCookieManage;
        private Random r = new Random();
        private static readonly ILog mLog = LogManager.GetLogger(typeof(WebSocketHandshake).Name);

        public WebSocketHandshake(Uri aUri, string aProtocol, int aVersion, WebSocketCookieManager aCookieManage)
        {
            this.mUri = aUri;
            this.mProtocol = aProtocol;
            this.mVersion = aVersion;
            this.mCookieManage = aCookieManage;

            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.INITIALIZING + WebSocketMessage.WEBSOCKET
                    + WebSocketMessage.HANDSHAKE);

            if (WebSocketProtocolAbstraction.isHybiVersion(aVersion))
                GenerateHybiKeys();
            else
                throw new WebSocketException(WebSocketMessage.WEBSOCKET
                    + WebSocketMessage.HANDSHAKE + WebSocketMessage.SEPARATOR
                    + WebSocketMessage.ILLEGAL_WEBSOCKET_PROTOCOL_VERSION + aVersion
                    + WebSocketMessage.DETECTED);
        }

        /// <summary>
        /// Generates the initial Handshake from a Client to the WebSocket.
        /// </summary>
        /// <returns>Handshake as byte array.</returns>
        public byte[] GenerateC2SRequest()
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.GENERATING_C2S_REQUEST);

            string lHost = mUri.DnsSafeHost;
            string lPath = mUri.PathAndQuery;

            mOrigin = WebSocketMessage.HTTP + lHost;

            if (string.Empty.Equals(lPath))
                lPath = WebSocketMessage.SLASH;

            string lHandshake =
                     WebSocketMessage.GET + lPath + WebSocketMessage.HTTP11
                   + WebSocketMessage.HOST + WebSocketMessage.TWO_POINT + lHost + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.UPGRADE_WEBSOCEKT + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.CONNECTION_UPGRADE + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.SEC_WEBSOCKET_KEY + WebSocketMessage.TWO_POINT
                   + mHybiKey + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.ORIGIN + WebSocketMessage.TWO_POINT + mOrigin + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.SEC_WEBSOCKET_PROTOCOL + WebSocketMessage.TWO_POINT + mProtocol
                   + WebSocketMessage.NL_RETURN
                   + WebSocketMessage.SEC_WEBSOCKET_VERSION + WebSocketMessage.TWO_POINT + mVersion;

            if (mCookieManage.Count() > 0)
                lHandshake += WebSocketMessage.NL_RETURN + mCookieManage.ProcessCookies(mCookieManage.GetCookies(mUri));

            lHandshake += WebSocketMessage.NL_RETURN + WebSocketMessage.NL_RETURN;
            return WebSocketConvert.StringToBytes(lHandshake, WebSocketTypeEncoding.ASCII);
        }
      
        private void GenerateHybiKeys()
        {
            Guid lUUID = Guid.NewGuid();
            byte[] lBA = lUUID.ToByteArray();
            mHybiKey = Convert.ToBase64String(lBA);
            mHybiKeyAccept = CalcHybiSecKeyAccept(mHybiKey);
        }

        private static string CalcHybiSecKeyAccept(string aKey)
        {
            aKey = aKey + WebSocketMessage.KEY;
            string lAccept = null;
            SHA1 lSHA1 = new SHA1CryptoServiceProvider();
            byte[] lBufSource = WebSocketConvert.StringToBytes(aKey, WebSocketTypeEncoding.Default);
            try
            {
                byte[] lBufTarget = lSHA1.ComputeHash(lBufSource);
                lAccept = Convert.ToBase64String(lBufTarget);
            }
            catch (Exception lEx)
            {
                throw new WebSocketException(WebSocketMessage.ERROR_CONVERTING_BASE64 + lEx.Message);
            }
            return lAccept;
        }

        /// <summary>
        /// Verify that the server's response is correct.
        /// </summary>
        /// <param name="aHeaders">Header with the response data.</param>
        public void VerifyS2CResponse(WebSocketHeaders aHeaders)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.VERIFYING_S2C_RESPONSE);

            if (!aHeaders.GetFirstLineResponse.Equals(WebSocketMessage.HTTP_SWITCHING_PROTOCOL))
                throw new WebSocketException(WebSocketMessage.CONNECTION_FAILED_HTTP);
            if (!aHeaders.GetResponseField(WebSocketConstants.UPGRADE).Equals(WebSocketMessage.WEBSOCKET1))
                throw new WebSocketException(WebSocketMessage.CONNECTION_FAILED_UPGRADE);
            if (!aHeaders.GetResponseField(WebSocketConstants.CONNECTION).Equals(WebSocketMessage.UPGRADE) &&
                !aHeaders.GetResponseField(WebSocketConstants.CONNECTION).Equals(WebSocketMessage.LOWUPGRADE))
                throw new WebSocketException(WebSocketMessage.CONNECTION_FAILED_CONNECTION);
            if (!aHeaders.GetResponseField(WebSocketConstants.SEC_WEBSOCKET_ACCEPT).Equals(mHybiKeyAccept))
                throw new WebSocketException(WebSocketMessage.CONNECTION_FAILED_SEC_WEBSOCKET_KEY);
            if (!aHeaders.GetResponseField(WebSocketConstants.SEC_WEBSOCKET_PROTOCOL).Equals(mProtocol))
                throw new WebSocketException(WebSocketMessage.CONNECTION_FAILED_SEC_WEBSOCKET_PROTOCOL);
        }
    }
}
