/*--------------------------------------------------------------------------------
 * jWebSocket - IToken
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

namespace WebSocket.org.jwebsocket.token.api
{

    /// <author>Rolando Betancourt Toucet</author>
    /// <lastUpdate>4/13/2013</lastUpdate>
    /// <summary>
    /// 
    /// </summary>
    public interface IToken
    {
        /// <summary>
        /// Copies all fields from a Map into the Token. A check has to be made
	    /// by the corresponding implementations that only such data types are
	    /// passed that are supported by the Token abstraction.
        /// </summary>
        /// <param name="aDictionary">Dictionary.</param>
        void SetDictionary(Dictionary<string, object> aDictionary);

        /// <summary>
        /// Gets the object.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        object GetObject(string aKey);

        /// <summary>
        /// Gets the string.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        string GetString(string aKey);

        /// <summary>
        /// Sets the string.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aValue">Value.</param>
        void SetString(string aKey, string aValue);

        /// <summary>
        /// Gets the int.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        int GetInt(string aKey);

        /// <summary>
        /// Sets the int.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aValue">Value.</param>
        void SetInt(string aKey, int aValue);

        /// <summary>
        /// Gets the double.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        double GetDouble(string aKey);

        /// <summary>
        /// Sets the double.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aValue">Value.</param>
        void SetDouble(string aKey, double aValue);

        /// <summary>
        /// Gets the bool.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        bool GetBool(string aKey);

        /// <summary>
        /// Sets the bool.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aValue">if set to <c>true</c> [a value].</param>
        void SetBool(string aKey, bool aValue);

        /// <summary>
        /// Gets the list.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns>List.</returns>
        List<object> GetList(string aKey);

        /// <summary>
        /// Sets the list.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aList">List.</param>
        void SetList(string aKey, List<object> aList);

        /// <summary>
        /// Sets the token.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aToken">Token.</param>
        void SetToken(string aKey, IToken aToken);

        /// <summary>
        /// Gets the token.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns></returns>
        IToken GetToken(string aKey);

        /// <summary>
        /// Gets the dictionary.
        /// </summary>
        /// <returns></returns>
        Dictionary<string, object> GetDictionary();

        /// <summary>
        /// Gets the dictionary.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <returns>Dictionary.</returns>
        Dictionary<string, object> GetDictionary(string aKey);

        /// <summary>
        /// Sets the dictionary.
        /// </summary>
        /// <param name="aKey">key.</param>
        /// <param name="aDictionary">Dictionary.</param>
        void SetDictionary(string aKey, Dictionary<string, object> aDictionary);

        /// <summary>
        /// Gets the type.
        /// </summary>
        /// <returns>Type.</returns>
        string GetType();

        /// <summary>
        /// Sets the type.
        /// </summary>
        /// <param name="aType">Type.</param>
        void SetType(string aType);

        /// <summary>
        /// Gets the NS.
        /// </summary>
        /// <returns>NS.</returns>
        string GetNS();

        /// <summary>
        /// Sets the NS.
        /// </summary>
        /// <param name="aNS">NS.</param>
        void SetNS(string aNS);

        /// <summary>
        /// Resets all fields of the token. After this operation the token is empty.
        /// </summary>
        void Clear();

        /// <summary>
        /// Removes the specified a key.
        /// </summary>
        /// <param name="aKey">key.</param>
        void Remove(string aKey);

        /// <summary>
        /// Determines whether this instance is binary.
        /// </summary>
        /// <returns><c>true</c> if this instance is binary; otherwise, <c>false</c>.</returns>
        bool IsBinary { get; set; }

    }
}
