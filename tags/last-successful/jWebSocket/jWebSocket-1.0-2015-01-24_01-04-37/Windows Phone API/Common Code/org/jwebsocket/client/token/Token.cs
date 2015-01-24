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

namespace ClientLibrary.org.jwebsocket.client.token
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public interface Token
    {

        string Type { get; set; }

        string Namespace { get; set; }

        void SetDictionary(Dictionary<string, object> aDictionary);

        object GetObject(string aKey);

        string GetString(string aKey);

        void SetString(string aKey, string aValue);

        int GetInt(string aKey);

        void SetInt(string aKey, int aValue);

        double GetDouble(string aKey);

        void SetDouble(string aKey, double aValue);

        bool GetBool(string aKey);

        void SetBool(string aKey, bool aValue);

        List<object> GetList(string aKey);

        void SetList(string aKey, List<object> aList);

        void SetToken(string aKey, Token aToken);

        Token GetToken(string aKey);

        Dictionary<string, object> GetDictionary();

        Dictionary<string, object> GetDictionary(string aKey);

        void SetDictionary(string aKey, Dictionary<string, object> aDictionary);

        void Clear();

        void Remove(string aKey);
    }
}
