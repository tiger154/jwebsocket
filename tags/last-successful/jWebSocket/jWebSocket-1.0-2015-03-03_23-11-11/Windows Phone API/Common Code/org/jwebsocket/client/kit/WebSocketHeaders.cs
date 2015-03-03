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
using System.Net.Sockets;
using System.Text.RegularExpressions;

namespace ClientLibrary.org.jwebsocket.client.kit
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketHeaders
    {
        private Dictionary<string, string> mRequestFields;
        private Dictionary<string, string> mResponseFields;
        private string mFirstLineRequest;
        private string mFirstLineResponse;

        public WebSocketHeaders()
        {
            mRequestFields = new Dictionary<string, string>();
            mResponseFields = new Dictionary<string, string>();
            mFirstLineRequest = null;
            mFirstLineResponse = null;
        }

        public void ReadResponseFromBuffer(byte[] aBuff)
        {
            int lHeaderComplete = 0;
            int lLines = 0;
            int lSizeBuff = aBuff.Length;

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
                        char[] c = { ':' };
                        string[] lKeyVal = lLine.Split(c);
                        if(lKeyVal.Count() == 2)
                            mResponseFields.Add(lKeyVal[0].Trim(), lKeyVal[1].Trim());
                    }
                    else
                        mFirstLineResponse = lLine;
                    lBuff = new byte[0];
                    lLines++;
                }
                else if (!lNL.Equals(0x0A) && !lNL.Equals(0x0D))
                    Write(ref lBuff, lNL);
                lHeaderComplete++;
            }
        }

        public void ReadHeadersFromResponseString(string response)
        {
            var headers = response.Split(new []{"\r\n"}, StringSplitOptions.RemoveEmptyEntries);
            mFirstLineResponse = headers[0];
            foreach (var headerLine in headers.Skip(1))
            {
                var headerKeyValue = headerLine.Split(new []{":"}, StringSplitOptions.None);
                mResponseFields.Add(headerKeyValue[0].Trim(), headerKeyValue[1].Trim());
            }
        }

        public void ReadRequestFromBuffer(byte[] aBuff)
        {
            int lHeaderComplete = 0;
            int lLines = 0;
            int lSizeBuff = aBuff.Length;

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
                        char[] c = { ':' };
                        string[] lKeyVal = lLine.Split(c);
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

        private int Read(Stream aSR)
        {
            try
            {
                return aSR.ReadByte();
            }
            catch (IOException lEx)
            {
                throw new WebSocketException("Error on reading stream: " + lEx.Message);
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
    }
}
