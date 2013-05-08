/*--------------------------------------------------------------------------------
 * jWebSocket - Token
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
using WebSocket.org.jwebsocket.token.api;
using WebSocket.org.jwebsocket.common;
using WebSocket.org.jwebsocket.token.processor;
using JSON;

namespace WebSocket.org.jwebsocket.token
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public class Token : IToken
    {
        private Dictionary<string, object> mData = null;
        private bool mBinary = false;

        public Token()
        {
            mData = new Dictionary<string, object>();
        }

        public Token(string aType)
        {
            mData = new Dictionary<string, object>();
            SetType(aType);
        }

        public Token(Dictionary<string, object> aDictionary)
        {
            mData = aDictionary;
        }

        public Token(string aNS, string aType)
        {
            mData = new Dictionary<string, object>();
            SetNS(aNS);
            SetType(aType);
        }

        public void SetDictionary(Dictionary<string, object> aDictionary)
        {
            mData = aDictionary;
        }

        public object GetObject(string aKey)
        {
            object lObj = null;
            try
            {
                lObj = Get(aKey);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_FIND_KEY + lEx.Message);
            }
            return lObj;
        }

        public string GetString(string aKey)
        {
            string lResult = String.Empty;
            try
            {
                lResult = (string)Get(aKey);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_FIND_KEY + lEx.Message);
            }
            return lResult;
        }

        public void SetString(string aKey, string aValue)
        {
            try
            {
                mData.Add(aKey, aValue);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public int GetInt(string aKey)
        {
            int lResult;
            try
            {
                lResult = (int)Get(aKey);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_FIND_KEY + lEx.Message);
            }
            return lResult;
        }

        public void SetInt(string aKey, int aValue)
        {
            try
            {
                mData.Add(aKey, aValue);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public double GetDouble(string aKey)
        {
            double lResult;
            try
            {
                object lObj = Get(aKey);
                if (lObj is string)
                {
                    lResult = double.Parse((string)lObj);
                }
                else if (lObj is int)
                {
                    lResult = (int)lObj / 1.0;
                }
                else
                {
                    lResult = (double)lObj;
                }
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_PARSED_VALUE + lEx.Message);
            }
            return lResult;
        }

        public void SetDouble(string aKey, double aValue)
        {
            try
            {
                mData.Add(aKey, aValue);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public bool GetBool(string aKey)
        {
            bool lResult;
            try
            {
                lResult = (bool)Get(aKey);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_PARSED_VALUE + lEx.Message);
            }
            return lResult;
        }

        public void SetBool(string aKey, bool aValue)
        {
            try
            {
                mData.Add(aKey, aValue);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public List<object> GetList(string aKey)
        {
            List<object> lResult = null;
            try
            {
                lResult = JSONTokenProcessor.JSonArrayToList(JSONTokenProcessor.JsonStringToJsonArray(Get(aKey).ToString()));
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_PARSED_VALUE + lEx.Message);
            }
            return lResult;
        }

        public void SetList(string aKey, List<object> aList)
        {
            try
            {
                mData.Add(aKey,aList);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public void SetToken(string aKey, IToken aToken)
        {
            try
            {
                mData.Add(aKey, aToken);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE+ lEx.Message);
            }
        }

        public IToken GetToken(string aKey)
        {
            IToken lResult = null;
            try
            {
                lResult = JSONTokenProcessor.JsonStringToToken(Get(aKey).ToString());
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_PARSED_VALUE + lEx.Message);
            }
            return lResult;
        }

        public Dictionary<string, object> GetDictionary()
        {
            return mData;
        }

        public Dictionary<string, object> GetDictionary(string aKey)
        {
            Dictionary<string, object> lResult = null;
            try
            {
                lResult = JSONTokenProcessor.JSonObjectToDictionary((JsonObject)Get(aKey));
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_PARSED_VALUE + lEx.Message);
            }
            return lResult;
        }

        public void SetDictionary(string aKey, Dictionary<string, object> aDictionary)
        {
            try
            {
                mData.Add(aKey, aDictionary);
            }
            catch (Exception lEx)
            {
                throw new Exception(WebSocketMessage.NO_INSERT_KEY_VALUE + lEx.Message);
            }
        }

        public string GetType()
        {
            return GetString(WebSocketMessage.TYPE);
        }

        public void SetType(string aType)
        {
            SetString(WebSocketMessage.TYPE, aType);
        }

        public string GetNS()
        {
            return GetString(WebSocketMessage.NS);
        }

        public void SetNS(string aNS)
        {
            SetString(WebSocketMessage.NS, aNS);
        }

        public void Clear()
        {
            mData.Clear();
        }

        public void Remove(string aKey)
        {
            mData.Remove(aKey);
        }

        public bool IsBinary
        {
            get { return mBinary; }
            set { mBinary = value; }
        }

        private object GetValue(object aValue)
        {
            if (aValue is Token)
            {
                aValue = ((Token)aValue).GetDictionary();
            }
            else
            {
                if (aValue is List<object>)
                {
                    List<object> lList = new List<object>();
                    foreach (object lItem in (List<object>)aValue)
                    {
                        lList.Add(GetValue(lItem));
                    }
                    aValue = lList;
                }
                else
                {
                    if (aValue is Dictionary<string, object>)
                    {
                        Dictionary<string, object> lMap = new Dictionary<string, object>();
                        foreach (KeyValuePair<string, object> kv in (Dictionary<string, object>)aValue)
                        {
                            lMap.Add(kv.Key, kv.Value);
                        }
                        aValue = lMap;
                    }
                    else
                    {
                        if (aValue is object[])
                        {
                            List<object> lLis = new List<object>();
                            object[] lo = (object[])aValue;
                            for (int i = 0; i < lo.Length; i++)
                            {
                                lLis.Add(GetValue(lo[i]));
                            }
                            aValue = lLis;
                        }
                    }
                }
            }
            return aValue;
        }

        private void Add(string aKey, object aValue)
        {
            mData.Add(aKey, GetValue(aValue));
        }

        private object Get(string aKey)
        {
            return mData[aKey];
        }

    }
}
