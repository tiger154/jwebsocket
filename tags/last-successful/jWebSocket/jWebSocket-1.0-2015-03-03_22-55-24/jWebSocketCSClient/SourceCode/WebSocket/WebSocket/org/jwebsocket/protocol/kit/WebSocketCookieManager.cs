/*--------------------------------------------------------------------------------
 * jWebSocket - WebSocketCookieManager
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
using System.Web;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.common;

namespace WebSocket.org.jwebsocket.protocol.kit
{
    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public class WebSocketCookieManager : IWebSocketCookiesManager
    {
        private HttpCookieCollection mCookieCollection;

        public WebSocketCookieManager()
        {
            mCookieCollection = new HttpCookieCollection();
        }

        public void AddCookies(List<string> aCookies, Uri aUri)
        {
            HttpCookie lCookie;
            for (int i = 0; i < aCookies.Count; i++)
            {
                lCookie = CreateCookie(aCookies[i], aUri);
                if (Validate(lCookie))
                    mCookieCollection.Set(lCookie);
            }
        }

        private bool Validate(HttpCookie aCookie)
        {
            for (int i = 0; i < mCookieCollection.Count; i++)
            {
                if (mCookieCollection[i].Domain.Equals(aCookie.Domain)
                    && mCookieCollection[i].Path.Equals(aCookie.Path)
                    && mCookieCollection[i].Secure.Equals(aCookie.Secure))
                {
                    return false;
                }
            }
            return true;
        }

        public HttpCookieCollection GetCookies(Uri aUri)
        {
            HttpCookieCollection lCookieCollection = new HttpCookieCollection();

            for (int i = 0; i < mCookieCollection.Count; i++)
            {
                if (((mCookieCollection[i].Expires.Equals(DateTime.MinValue))
                    || ((!mCookieCollection[i].Expires.Equals(DateTime.MinValue)
                    && mCookieCollection[i].Expires > DateTime.Now)))
                    && mCookieCollection[i].Secure == aUri.Scheme.Equals("wss")
                    && BuildPath(aUri.AbsolutePath).StartsWith(mCookieCollection[i].Path)
                    && aUri.DnsSafeHost.ToString().EndsWith(mCookieCollection[i].Domain))
                {
                    lCookieCollection.Add(mCookieCollection[i]);
                }
            }

            return lCookieCollection;
        }

        public string ProcessCookies(HttpCookieCollection aCookies)
        {
            StringBuilder lStrBuild = new StringBuilder();
            lStrBuild.Append(WebSocketMessage.COOKIE);

            for (int i = 0; i < aCookies.Count; i++)
            {
                lStrBuild.Append(WebSocketMessage.TWO_POINT).Append(aCookies[i].Name)
                    .Append(WebSocketMessage.EQUAL).Append(aCookies[i].Value);
            }
            return lStrBuild.ToString();
        }

        public int Count()
        {
            return mCookieCollection.Count;
        }

        private HttpCookie CreateCookie(string aCookie, Uri aUri)
        {
            char[] lTwoPoint = { WebSocketMessage.TWO_POINT2 };
            string[] lKeyVal = aCookie.Split(lTwoPoint, 2);
            string[] lArgs = lKeyVal[1].Split(WebSocketMessage.SEMICOLON);

            HttpCookie lCookie = new HttpCookie(lArgs[0].TrimStart().Split('=')[0], lArgs[0].TrimStart().Split('=')[1]);

            for (int i = 0; i < lArgs.Length; i++)
            {
                if (lArgs[i].Contains(WebSocketMessage.EQUAL))
                {
                    string[] lKeyValue = lArgs[i].TrimStart().Split('=');
                    if (lKeyValue[0].Equals(WebSocketMessage.EXPIRES))
                        lCookie.Expires = DateTime.Parse(lKeyValue[1]);
                    else if (lKeyValue[0].Equals(WebSocketMessage.DOMAIN))
                        lCookie.Domain = lKeyValue[1];
                    else if (lKeyValue[0].Equals(WebSocketMessage.PATH))
                        lCookie.Path = lKeyValue[1];
                }
                else
                {
                    if (lArgs[i].TrimStart().Equals(WebSocketMessage.SECURE))
                        lCookie.Secure = true;
                    else if (lArgs[i].TrimStart().Equals(WebSocketMessage.HTTPONLY))
                        lCookie.HttpOnly = true;
                }
            }
            if (aUri.Scheme.ToString().Equals(WebSocketMessage.WSS))
                lCookie.Secure = true;

            if (lCookie.Domain == null)
                lCookie.Domain = aUri.DnsSafeHost;

            if (lCookie.Path.Equals(WebSocketMessage.SLASH))
                lCookie.Path = BuildPath(aUri.AbsolutePath);

            return lCookie;
        }

        private string BuildPath(string aPath)
        {
            StringBuilder lStrBuild = new StringBuilder();
            char[] lSlash = { WebSocketMessage.SLASHCHAR, WebSocketMessage.SLASHCHAR };
            string[] lPath = aPath.Split(lSlash);

            for (int i = 0; i < lPath.Length; i++)
            {
                if (!lPath[i].Equals(string.Empty))
                    lStrBuild.Append(WebSocketMessage.SLASHCHAR).Append(lPath[i]);
            }
            return lStrBuild.ToString();
        }

    }
}
