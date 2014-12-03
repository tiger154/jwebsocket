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
using ClientLibrary.org.jwebsocket.client.api;

namespace ClientLibrary.org.jwebsocket.client.kit
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketRawPacket : WebSocketPacket
    {
        private byte[] mByteArray = null;
        private bool mIsEndFrame = false;
        private WebSocketFrameType mFrameType = WebSocketFrameType.TEXT;

        public WebSocketRawPacket(byte[] aByteArray)
        {
            mByteArray = aByteArray;
        }

        public WebSocketRawPacket(WebSocketFrameType aFrameType, byte[] aByteArray)
        {
            mFrameType = aFrameType;
            mByteArray = aByteArray;
        }

        public WebSocketRawPacket(string aString)
        {
            SetString(aString);
        }

        public WebSocketRawPacket(WebSocketFrameType aFrameType, string aString)
        {
            mFrameType = aFrameType;
            SetString(aString);
        }

        public WebSocketRawPacket(string aString, WebSocketTypeEncoding aEncoding)
        {
            SetString(aString, aEncoding);
        }

        public void SetString(string aString)
        {
            mByteArray = WebSocketConvert.StringToBytes(aString, WebSocketTypeEncoding.Default);
        }

        public void SetString(string aString, WebSocketTypeEncoding aEncoding)
        {
            mByteArray = WebSocketConvert.StringToBytes(aString, aEncoding);
        }

        public void SetUTF8(string aString)
        {
            mByteArray = WebSocketConvert.StringToBytes(aString, WebSocketTypeEncoding.UTF8);
        }

        public string GetString()
        {
            return WebSocketConvert.BytesToString(mByteArray, WebSocketTypeEncoding.Default);
        }

        public string GetString(WebSocketTypeEncoding aEncoding)
        {
            return WebSocketConvert.BytesToString(mByteArray, aEncoding);
        }

        public string GetUTF8()
        {
            return WebSocketConvert.BytesToString(mByteArray, WebSocketTypeEncoding.UTF8);
        }

#if !WINDOWS_PHONE

        public void SetASCII(string aString)
        {
            mByteArray = WebSocketConvert.StringToBytes(aString, WebSocketTypeEncoding.ASCII);
        }

        public string GetASCII()
        {
            return WebSocketConvert.BytesToString(mByteArray, WebSocketTypeEncoding.ASCII);
        }
#endif

        public bool IsEndFrame()
        {
            return mIsEndFrame;
        }

        public void SetEndFrame(bool aEndFrame)
        {
            mIsEndFrame = aEndFrame;
        }

        public WebSocketFrameType GetFrameType()
        {
            return mFrameType;
        }

        public void SetFrameType(WebSocketFrameType aFrameType)
        {
            mFrameType = aFrameType;
        }

        public byte[] GetByteArray()
        {
            return mByteArray;
        }

        public void SetByteArray(byte[] aByteArray)
        {
            mByteArray = aByteArray;
        }
    }
}
