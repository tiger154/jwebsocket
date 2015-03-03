//	---------------------------------------------------------------------------
//	jWebSocket - ParcelableToken (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.android.library;

import android.os.Parcel;
import android.os.Parcelable;
import org.jwebsocket.token.Token;

/**
 *
 * @author Alexander Schulze
 */
public class ParcelableToken implements Parcelable {

	/**
	 *
	 */
	public static final Parcelable.Creator<ParcelableToken> CREATOR = new Parcelable.Creator<ParcelableToken>() {
		@Override
		public ParcelableToken createFromParcel(Parcel in) {
			return new ParcelableToken(in);
		}

		@Override
		public ParcelableToken[] newArray(int size) {
			return new ParcelableToken[size];
		}
	};

	/**
	 *
	 * @param in
	 */
	public ParcelableToken(Parcel in) {
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 *
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	/**
	 *
	 * @return
	 */
	public Token getToken() {
		return null;
	}
}
