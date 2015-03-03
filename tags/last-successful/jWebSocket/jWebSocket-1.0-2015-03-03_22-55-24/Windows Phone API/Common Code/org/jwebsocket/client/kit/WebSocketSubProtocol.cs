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
using ClientLibrary.org.jwebsocket.client.config;

namespace ClientLibrary.org.jwebsocket.client.kit
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketSubProtocol
    {
        private string mSubProt;
        private string mNameSpace;
        private string mFormat;
        private WebSocketEncoding mEncoding;

        public WebSocketSubProtocol(string aSubProt, WebSocketEncoding aEncoding)
        {
            this.mSubProt = aSubProt;
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
            return mSubProt.GetHashCode() + (int)mEncoding;
        }

        public override bool Equals(object aObj)
        {
            if (aObj is WebSocketSubProtocol)
            {
                WebSocketSubProtocol lOther = (WebSocketSubProtocol)aObj;
                return mSubProt.Equals(lOther.mSubProt) && mEncoding.Equals(lOther.mEncoding);
            }
            else
                return base.Equals(aObj);
        }

        public override string ToString()
        {
            StringBuilder lBuff = new StringBuilder();
            lBuff.Append(mSubProt).Append('[').Append(mEncoding.ToString()).Append("]");
            return lBuff.ToString();
        }

        public string GetSubProt()
        {
            return mSubProt;
        }

        public WebSocketEncoding GetEncoding()
        {
            return mEncoding;
        }

        public string GetNameSpace()
        {
            return mNameSpace;
        }

        public string GetFormat()
        {
            return mFormat;
        }
    }
}
