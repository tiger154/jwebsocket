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

namespace ClientLibrary.org.jwebsocket.client.kit
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketConvert
    {
        public static byte[] StringToBytes(string aString, WebSocketTypeEncoding aEncoding)
        {
            try
            {
                byte[] lData = null;
                switch (aEncoding)
                {
                    #if !WINDOWS_PHONE
                    case WebSocketTypeEncoding.ASCII:
                        lData = ASCIIEncoding.ASCII.GetBytes(aString);
                        break;
                   
                    case WebSocketTypeEncoding.UTF32:
                        lData = ASCIIEncoding.UTF32.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.UTF7:
                        lData = System.Text.Encoding..UTF7.GetBytes(aString);
                        break;
                    #endif
                    case WebSocketTypeEncoding.UTF8:
                        lData =System.Text.Encoding.UTF8.GetBytes(aString);
                        break;
                    case WebSocketTypeEncoding.Unicode:
                        lData = System.Text.Encoding.Unicode.GetBytes(aString);
                        break;
                    default:
                        #if !WINDOWS_PHONE
                        lData = ASCIIEncoding.Default.GetBytes(aString);
                        #else
                        lData = System.Text.Encoding.UTF8.GetBytes(aString);
                        #endif
                        break;
                }
                return lData;
            }
            catch (Exception lEx)
            {
                throw new Exception("invalid converting : " + lEx.Message);
            }
        }

        public static string BytesToString(byte [] aBytes, WebSocketTypeEncoding aEncoding)
        {
            try
            {
                var lString = string.Empty;
                switch (aEncoding)
                {
#if !WINDOWS_PHONE
                    case WebSocketTypeEncoding.ASCII:
                        lString = ASCIIEncoding.ASCII.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.UTF32:
                        lString = ASCIIEncoding.UTF32.GetString(aBytes);
                        break;
                    case WebSocketTypeEncoding.UTF7:
                        lString = ASCIIEncoding.UTF7.GetString(aBytes);
                        break;
#endif
                    case WebSocketTypeEncoding.Unicode:
                        lString = System.Text.Encoding.Unicode.GetString(aBytes, 0, aBytes.Length);
                        break;
                    
                    case WebSocketTypeEncoding.UTF8:
                        lString = System.Text.Encoding.UTF8.GetString(aBytes, 0, aBytes.Length);
                        break;
                    default:
#if !WINDOWS_PHONE
                        lString = ASCIIEncoding.Default.GetString(aBytes);
#endif
                        lString = System.Text.Encoding.Unicode.GetString(aBytes, 0, aBytes.Length);
                        break;
                }
                return lString;
            }
            catch (Exception lEx)
            {
                throw new Exception("invalid converting : " + lEx.Message);
            }
        }
    }
}
