/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketHeaders
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
using System.Net.Sockets;
using log4net;
using log4net.Config;
using WebSocket.org.jwebsocket.common;

namespace WebSocket.org.jwebsocket.protocol.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2012</lastUpdate>
    /// <summary>
    /// Implementation of the request and response headers
    /// </summary>
    public class WebSocketHeaders
    {
        private Dictionary<string, string> mRequestFields;
        private Dictionary<string, string> mResponseFields;
        private List<string> mCookies;
        private string mFirstLineRequest;
        private string mFirstLineResponse;
        private static readonly ILog mLog = LogManager.GetLogger(typeof(WebSocketHeaders).Name);

        public WebSocketHeaders()
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.INITIALIZING + WebSocketMessage.WEBSOCKET
                    + WebSocketMessage.HEADERS);

            mRequestFields = new Dictionary<string, string>();
            mResponseFields = new Dictionary<string, string>();
            mCookies = new List<string>();
            mFirstLineRequest = null;
            mFirstLineResponse = null;
        }

        /// <summary>
        /// Reads the response from stream.
        /// </summary>
        /// <param name="aSR">Data stream from server.</param>
        public void ReadResponseFromStream(NetworkStream aSR)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.READING_RESPONSE_FROM_STREAM);

            bool lHeaderComplete = false;
            int lRT = -1;
            int lNL = -1;
            int lLines = 0;
            byte[] lBuff = new byte[0];

            while (!lHeaderComplete)
            {
                lRT = lNL;
                lNL = Read(aSR);

                if (lRT.Equals(0x0D) && lNL.Equals(0x0A))
                {
                    string lLine = WebSocketConvert.BytesToString(lBuff, WebSocketTypeEncoding.UTF8);
                    if (lLines > 0)
                    {
                        if (!string.Empty.Equals(lLine))
                        {
                            char[] c = { WebSocketMessage.TWO_POINT2 };
                            string[] lKeyVal = lLine.Split(c, 2);
                            if (lKeyVal[0].Equals(WebSocketMessage.SET_COOKIE))
                                mCookies.Add(lLine);
                            else
                                mResponseFields.Add(lKeyVal[0].Trim(), lKeyVal[1].Trim());
                        }
                        else
                            lHeaderComplete = true;
                    }
                    else
                        mFirstLineResponse = lLine;
                    lLines++;
                    lBuff = new byte[0];
                }
                else if (!lNL.Equals(0x0A) && !lNL.Equals(0x0D))
                    Write(ref lBuff, lNL);
            }
        }

        /// <summary>
        /// Reads the request from buffer.
        /// </summary>
        /// <param name="aBuff">Data buffer from client.</param>
        public void ReadRequestFromBuffer(byte[] aBuff)
        {
            if (mLog.IsDebugEnabled)
                mLog.Debug(WebSocketMessage.READING_REQUEST_FROM_BUFFER);

            int lHeaderComplete = 0;
            int lLines = 0;
            int lSizeBuff = aBuff.Length - 2;

            byte lRT = 0;
            byte lNL = 0;
            byte[] lBuff = new byte[0];

            while (lHeaderComplete < lSizeBuff)
            {
                lRT = lNL;
                lNL = aBuff[lHeaderComplete];
                if (lRT.Equals(0x0D) && lNL.Equals(0x0A))
                {
                    string lLine = WebSocketConvert.BytesToString(lBuff, WebSocketTypeEncoding.UTF8);
                    if (lLines > 0)
                    {
                        char[] c = { WebSocketMessage.TWO_POINT2 };
                        string[] lKeyVal = lLine.Split(c, 2);
                        if (lKeyVal.Length == 2)
                            mRequestFields.Add(lKeyVal[0].Trim(), lKeyVal[1].Trim());
                    }
                    else
                        mFirstLineRequest = lLine;
                    lBuff = new byte[0];
                    lLines++;
                }
                else if (!lNL.Equals(0x0A) && !lNL.Equals(0x0D))
                    Write(ref lBuff, lNL);
                lHeaderComplete++;
            }
        }

        private int Read(NetworkStream aSR)
        {
            try
            {
                return aSR.ReadByte();
            }
            catch (IOException lEx)
            {
                throw new WebSocketException(WebSocketMessage.ERROR_READING_STREAM
                    + WebSocketMessage.TWO_POINT + lEx.Message);
            }
        }

        private void Write(ref byte[] aBuff, int aValue)
        {
            byte[] lBuff = new byte[aBuff.Length + 1];
            Array.Copy(aBuff, lBuff, aBuff.Length);
            lBuff[lBuff.Length - 1] = (byte)aValue;
            aBuff = lBuff;
        }

        public Dictionary<string, string> GetRequestFields
        {
            get { return mRequestFields; }
        }

        public Dictionary<string, string> GetResponseFields
        {
            get { return mResponseFields; }
        }

        public List<string> GetCookies
        {
            get { return mCookies; }
        }

        public string GetFirstLineRequest
        {
            get { return mFirstLineRequest; }
        }

        public string GetFirstLineResponse
        {
            get { return mFirstLineResponse; }
        }

        public string GetRequestField(string afield)
        {
            if (mRequestFields.Count > 0)
                return mRequestFields[afield];
            else
                return null;
        }

        public string GetResponseField(string afield)
        {
            if (mResponseFields.Count > 0)
                return mResponseFields[afield];
            else
                return null;
        }

        /// <summary>
        /// Gets request as string.
        /// </summary>
        /// <returns>String request</returns>
        public string ToStringRequest()
        {
            return mFirstLineRequest
                + WebSocketMessage.SLASH1 + WebSocketConstants.HOST + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.HOST]
                + WebSocketMessage.SLASH1 + WebSocketConstants.UPGRADE + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.UPGRADE]
                + WebSocketMessage.SLASH1 + WebSocketConstants.CONNECTION + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.CONNECTION]
                + WebSocketMessage.SLASH1 + WebSocketConstants.SEC_WEBSOCKET_KEY + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.SEC_WEBSOCKET_KEY]
                + WebSocketMessage.SLASH1 + WebSocketConstants.ORIGIN + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.ORIGIN]
                + WebSocketMessage.SLASH1 + WebSocketConstants.SEC_WEBSOCKET_PROTOCOL + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.SEC_WEBSOCKET_PROTOCOL]
                + WebSocketMessage.SLASH1 + WebSocketConstants.SEC_WEBSOCKET_VERSION + WebSocketMessage.TWO_POINT
                + mRequestFields[WebSocketConstants.SEC_WEBSOCKET_VERSION];
        }

        /// <summary>
        /// Gets response as string.
        /// </summary>
        /// <returns>String response</returns>
        public string ToStringResponse()
        {
            return mFirstLineResponse
                + WebSocketMessage.SLASH1 + WebSocketConstants.UPGRADE + WebSocketMessage.TWO_POINT
                + mResponseFields[WebSocketConstants.UPGRADE]
                + WebSocketMessage.SLASH1 + WebSocketConstants.CONNECTION + WebSocketMessage.TWO_POINT
                + mResponseFields[WebSocketConstants.CONNECTION]
                + WebSocketMessage.SLASH1 + WebSocketConstants.SEC_WEBSOCKET_ACCEPT + WebSocketMessage.TWO_POINT
                + mResponseFields[WebSocketConstants.SEC_WEBSOCKET_ACCEPT]
                + WebSocketMessage.SLASH1 + WebSocketConstants.SEC_WEBSOCKET_PROTOCOL + WebSocketMessage.TWO_POINT
                + mResponseFields[WebSocketConstants.SEC_WEBSOCKET_PROTOCOL];
        }
    }
}
