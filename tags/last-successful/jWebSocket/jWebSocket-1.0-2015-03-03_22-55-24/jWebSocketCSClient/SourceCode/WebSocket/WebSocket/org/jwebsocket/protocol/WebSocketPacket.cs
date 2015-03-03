/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketPacket
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
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.protocol.kit;
using WebSocket.org.jwebsocket.common;

namespace WebSocket.org.jwebsocket.protocol
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// Implements the low level data packets which are interchanged between
    /// client and server. Data packets do not have a special format at this
    /// communication level.
    /// </summary>
    public sealed class WebSocketPacket : IWebSocketPacket 
    {
        private byte[] mByteArray = null;
        private string[] mFragments = null;

        private int mFragmentsLoaded = 0;
        private int mFragmentsExpected = 0;

        private bool mIsFragmented = false;
        private bool mIsComplete = false;

        private DateTime mCreationDate = DateTime.Now;
        private long mTimeout = 0;
        private WebSocketFrameType mFrameType = WebSocketFrameType.TEXT;

        public WebSocketPacket(int aInitialSize)
        {
            InitFragmented(aInitialSize);
        }

        public WebSocketPacket(byte[] aByteArray)
        {
            mByteArray = aByteArray;
        }

        public WebSocketPacket(WebSocketFrameType aFrameType, byte[] aByteArray)
        {
            mFrameType = aFrameType;
            mByteArray = aByteArray;
        }

        public WebSocketPacket(string aString)
        {
            SetString(aString);
        }

        public WebSocketPacket(WebSocketFrameType aFrameType, string aString)
        {
            mFrameType = aFrameType;
            SetString(aString);
        }

        public WebSocketPacket(string aString, WebSocketTypeEncoding aEncoding)
        {
            SetString(aString, aEncoding);
        }

        public void InitFragmented(int aTotal)
        {
            mFragmentsExpected = aTotal;
            mFragments = new string[aTotal];
        }

        public void SetFragment(string aString, int aIdx)
        {
            mFragments[aIdx] = aString;
            mFragmentsLoaded++;
            mIsComplete = mFragmentsLoaded >= mFragmentsExpected;
        }

        public void PackFragments()
        {
            StringBuilder lSB = new StringBuilder();
            for (int i = 0; i < mFragments.Length; i++)
            {
                lSB.Append(mFragments[i]);
                mFragments[i] = null;
            }
            mByteArray = WebSocketConvert.StringToBytes(lSB.ToString(), WebSocketTypeEncoding.UTF8);
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

        public void SetASCII(string aString)
        {
            mByteArray = WebSocketConvert.StringToBytes(aString, WebSocketTypeEncoding.ASCII);
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
            try
            {
                return WebSocketConvert.BytesToString(mByteArray, WebSocketTypeEncoding.UTF8);
            }
            catch (Exception)
            {
                return null;
            }
        }

        public string GetASCII()
        {
            try
            {
                return WebSocketConvert.BytesToString(mByteArray, WebSocketTypeEncoding.ASCII);
            }
            catch (Exception)
            {
                return null;
            }
        }

        public WebSocketFrameType FrameType
        {
            get { return mFrameType; }
            set { mFrameType = value; }
        }

        public byte[] ByteArray
        {
            get { return mByteArray; }
            set { mByteArray = value; }
        }

        public string[] Fragments
        {
            get { return mFragments; }
            set { mFragments = value; }
        }

        public bool IsFragmented
        {
            get { return mIsFragmented; }
            set { mIsFragmented = value; }
        }

        public bool IsComplete
        {
            get { return mIsComplete; }
        }

        public DateTime CreationDate
        {
            get { return mCreationDate; }
            set { mCreationDate = value; }
        }

        public long Timeout
        {
            get { return mTimeout; }
            set { mTimeout = value; }
        }

    }
}
