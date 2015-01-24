/*--------------------------------------------------------------------------------
 * jWebSocket - JSONTokenProcessor
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
using JSON;
using log4net;
using WebSocket.org.jwebsocket.token.api;
using WebSocket.org.jwebsocket.protocol.api;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.token;
using WebSocket.org.jwebsocket.protocol;

namespace WebSocket.org.jwebsocket.token.processor
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public class JSONTokenProcessor
    {
        private static readonly ILog mLog = LogManager.GetLogger(typeof(JSONTokenProcessor).Name);

        public static IWebSocketPacket TokenToPacket(IToken aToken)
        {
            try
            {
                Dictionary<string, object> lDictionary = aToken.GetDictionary();
                JsonObject lJson = new JsonObject();

                foreach (KeyValuePair<string, object> item in lDictionary)
                {
                    lJson.Add(item.Key, ConvertObjectToJsonObject(item.Value));
                }
                Console.WriteLine(lJson.ToString());
                return new WebSocketPacket(lJson.ToString());
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static IToken PacketToToken(IWebSocketPacket aDataPacket)
        {
            try
            {
                JsonObject json=new JsonObject(aDataPacket.GetString());
                Dictionary<string, object> lDictionary = new Dictionary<string, object>();

                for (int i = 0; i < json.Count; i++)
                    lDictionary.Add(json.Keys.ElementAt(i), json.Values.ElementAt(i));

                return new Token(lDictionary);
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static JsonObject TokenToJsonObject(IToken aToken)
        {
            try
            {
                Dictionary<string, object> lDictionary = aToken.GetDictionary();
                JsonObject lJson = new JsonObject();
                foreach (KeyValuePair<string, object> item in lDictionary)
                {
                    lJson.Add(item.Key, item.Value);
                }
                return lJson;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static JsonObject DictionaryToJsonObject(Dictionary<string, object> aDictionary)
        {
            try
            {
                JsonObject lJson = new JsonObject();
                foreach (KeyValuePair<string, object> item in aDictionary)
                {
                    lJson.Add(item.Key, item.Value);
                }
                return lJson;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static JsonArray ListToJsonArray(List<object> aList)
        {
            try
            {
                JsonArray lJsonArray = new JsonArray();
                foreach (object lItem in aList)
                {
                    lJsonArray.Add(ConvertObjectToJsonObject(lItem));
                }
                return lJsonArray;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static List<object> JSonArrayToList(JsonArray aJsonArray)
        {
            try
            {
                List<object> lList = new List<object>();
                for (int i = 0; i < aJsonArray.Count; i++)
                {
                    lList.Add(ConvertJsonToObject(aJsonArray[i]));
                }
                return lList;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        private static Object ConvertJsonToObject(object aObject)
        {
            if (aObject is JsonArray)
                return JSonArrayToList((JsonArray)aObject);
            else if (aObject is JsonObject)
                return JSonObjectToDictionary((JsonObject)aObject);
            else
                return aObject;
        }

        public static IToken JsonStringToToken(string aJsonString)
        {
            try
            {
                JsonObject json = new JsonObject(aJsonString);
                Dictionary<string, object> lDictionary = new Dictionary<string, object>();

                for (int i = 0; i < json.Count; i++)
                    lDictionary.Add(json.Keys.ElementAt(i), json.Values.ElementAt(i));

                return new Token(lDictionary);
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static object ConvertObjectToJsonObject(object aObject)
        {
            try
            {
                if (aObject is List<object>)
                    return ListToJsonArray((List<object>)aObject);
                else if (aObject is IToken)
                    return TokenToJsonObject((IToken)aObject);
                else if (aObject is Dictionary<string, object>)
                    return DictionaryToJsonObject((Dictionary<string, object>)aObject);
                else if (aObject is object[])
                    return ObjectListToJsonArray((object[])aObject);
                else if (aObject is String)
                    return (string)aObject;
                else if (aObject is Double)
                    return (double)aObject;
                else if (aObject is Boolean)
                    return (bool)aObject;
                else if (aObject is int)
                    return (int)aObject;
                else
                    return aObject;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static JsonArray ObjectListToJsonArray(object[] aObjectList)
        {
            try
            {
                JsonArray lArray = new JsonArray();
                for (int i = 0; i < aObjectList.Length; i++)
                {
                    lArray.Add(ConvertObjectToJsonObject(aObjectList[i]));
                }
                return lArray;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static Dictionary<string, object> JSonObjectToDictionary(JsonObject aJSonObject)
        {
            try
            {
                Dictionary<string, object> lDictionary = new Dictionary<string, object>();

                for (int i = 0; i < aJSonObject.Count; i++)
                    lDictionary.Add(aJSonObject.Keys.ElementAt(i), aJSonObject.Values.ElementAt(i));

                return lDictionary;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }

        public static JsonArray JsonStringToJsonArray(string aJsonString)
        {
            try
            {
                JsonArray lJsonArray = new JsonArray();

                aJsonString = aJsonString.Remove(0,1);
                aJsonString = aJsonString.Remove(aJsonString.Length - 1,1);
                object[] lList = aJsonString.Split(',');
                for (int i = 0; i < lList.Length; i++)
                {
                    lJsonArray.Add(lList[i]);
                }
                return lJsonArray;
            }
            catch (Exception lEx)
            {
                if (mLog.IsErrorEnabled)
                    mLog.Error(lEx.Source + WebSocketMessage.SEPARATOR + lEx.Message);
                return null;
            }
        }
    }
}
