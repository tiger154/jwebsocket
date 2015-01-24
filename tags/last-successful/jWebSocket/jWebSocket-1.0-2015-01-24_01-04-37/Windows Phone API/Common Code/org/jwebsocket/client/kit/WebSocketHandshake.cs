//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Cryptography;
using ClientLibrary.org.jwebsocket.client.config;


namespace ClientLibrary.org.jwebsocket.client.kit
{

    public class WebSocketHandshake
    {
        private Uri mUri = null;
        private string mHybiKey = null;
        private string mHybiKeyAccept = null;
        private string mOrigin = null;
        private string mProtocol = null;
        private int mVersion;
        private Random r = new Random();

        public WebSocketHandshake(Uri aUri, string aProtocol,int aVersion)
        {
            this.mUri = aUri;
            this.mProtocol = aProtocol;
            this.mVersion = aVersion;

            if (WebSocketProtocolAbstraction.isHybiVersion(aVersion))
                GenerateHybiKeys();
            else
                throw new WebSocketException("WebSocket handshake: Illegal WebSocket protocol version '"
                      + aVersion + "' detected.");
        }

        public byte[] GenerateC2SRequest()
        {
            string lHost = mUri.DnsSafeHost;
            //string lPath = mUri.PathAndQuery;
            string lPath = mUri.AbsolutePath + "?" + mUri.Query;
            mOrigin = "http://" + lHost;

            if ("".Equals(lPath))
                lPath = "/";

            string lHandshake =
                     "GET " + lPath + " HTTP/1.1 \r\n"
                   + "Host: " + lHost + "\r\n"
                   + "Upgrade: WebSocket\r\n"
                   + "Connection: Upgrade\r\n"
                   + "Sec-WebSocket-Key: " + mHybiKey + "\r\n"
                   + "Origin: " + mOrigin + "\r\n"
                   + "Sec-WebSocket-Protocol: " + mProtocol + "\r\n"
                   + "Sec-WebSocket-Version: " + mVersion + "\r\n";

            return WebSocketConvert.StringToBytes(lHandshake, WebSocketTypeEncoding.UTF8);
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
            aKey = aKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            string lAccept = null;
            //SHA1 lSHA1 = new SHA1CryptoServiceProvider();
            var sha1 = new SHA1Managed();
            byte[] lBufSource = WebSocketConvert.StringToBytes(aKey, WebSocketTypeEncoding.UTF8);
            try
            {
                //byte[] lBufTarget = lSHA1.ComputeHash(lBufSource);
                byte[] lBufTarget = sha1.ComputeHash(lBufSource);
                lAccept = Convert.ToBase64String(lBufTarget);
            }
            catch (Exception lEx) 
            {
                throw new WebSocketException("Error converting to Base64" + lEx.Message);
            }
            return lAccept;
        }

        public void VerifyS2CResponse(WebSocketHeaders aHeaders)
        {
            if (!aHeaders.GetFirstLineResponse.Equals("HTTP/1.1 101 Switching Protocols"))
                throw new WebSocketException("connection failed: missing header field in server handshake: HTTP/1.1");
            if (!aHeaders.GetResponseField(WebSocketConstants.UPGRADE).Equals("websocket"))
                throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
            if (!aHeaders.GetResponseField(WebSocketConstants.CONNECTION).Equals("Upgrade"))
                throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
            if (!aHeaders.GetResponseField(WebSocketConstants.SEC_WEBSOCKET_ACCEPT).Equals(mHybiKeyAccept))
                throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Key");
            if (!aHeaders.GetResponseField(WebSocketConstants.SEC_WEBSOCKET_PROTOCOL).Equals(mProtocol))
                throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Protocol");
        }
    }
}
