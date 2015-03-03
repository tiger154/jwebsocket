/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketSubProtocol
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
using WebSocket.org.jwebsocket.common;

namespace WebSocket.org.jwebsocket.protocol.kit
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Define WebSocket sub protocol.
    /// </summary>
    public class WebSocketSubProtocol
    {
        private string mSubProtocol;
        private string mNameSpace;
        private string mFormat;
        private WebSocketEncoding mEncoding;

      
        public WebSocketSubProtocol(string aSubProt, WebSocketEncoding aEncoding)
        {
            this.mSubProtocol = aSubProt;
            this.mEncoding = aEncoding;

            if (WebSocketConstants.WS_SUBPROT_JSON.Equals(aSubProt))
            {
                mNameSpace = WebSocketConstants.WS_SUBPROT_PREFIX;
                mFormat = WebSocketConstants.WS_FORMAT_JSON;
            }
            else if (WebSocketConstants.WS_SUBPROT_XML.Equals(aSubProt))
            {
                mNameSpace = WebSocketConstants.WS_SUBPROT_PREFIX;
                mFormat = WebSocketConstants.WS_FORMAT_XML;
            }
            else if (WebSocketConstants.WS_SUBPROT_CSV.Equals(aSubProt))
            {
                mNameSpace = WebSocketConstants.WS_SUBPROT_PREFIX;
                mFormat = WebSocketConstants.WS_FORMAT_CSV;
            }
            else if (WebSocketConstants.WS_SUBPROT_TEXT.Equals(aSubProt))
            {
                mNameSpace = WebSocketConstants.WS_SUBPROT_PREFIX;
                mFormat = WebSocketConstants.WS_FORMAT_TEXT;
            }
            else if (WebSocketConstants.WS_SUBPROT_BINARY.Equals(aSubProt))
            {
                mNameSpace = WebSocketConstants.WS_SUBPROT_PREFIX;
                mFormat = WebSocketConstants.WS_FORMAT_BINARY;
            }
        }

        public override int GetHashCode()
        {
            return mSubProtocol.GetHashCode() + (int)mEncoding;
        }

        public override bool Equals(object aObj)
        {
            if (aObj is WebSocketSubProtocol)
            {
                WebSocketSubProtocol lOther = (WebSocketSubProtocol)aObj;
                return mSubProtocol.Equals(lOther.mSubProtocol) && mEncoding.Equals(lOther.mEncoding);
            }
            else
                return base.Equals(aObj);
        }

        public override string ToString()
        {
            StringBuilder lBuff = new StringBuilder();
            lBuff.Append(mSubProtocol).Append(WebSocketMessage.L_PARENT).Append(mEncoding.ToString())
                .Append(WebSocketMessage.R_PARENT);
            return lBuff.ToString();
        }

        public string SubProtocol
        {
            get { return mSubProtocol; }
        }

        public string NameSpace
        {
            get { return mNameSpace; }
        }

        public string Format
        {
            get { return mFormat; }
        }

        public WebSocketEncoding Encoding
        {
            get { return mEncoding; }
        }

    }
}
