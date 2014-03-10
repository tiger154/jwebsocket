//	---------------------------------------------------------------------------
//	jWebSocket ChunkableInputStream (Community Edition, CE)
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
package org.jwebsocket.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The class implements the IChunkable interface to support the transmission of
 * data from a InputStream to the client
 *
 * @author Rolando Santamaria Maso, Alexander Schulze
 */
public class ChunkableInputStream extends BaseChunkable {

	private final InputStream mIS;
	private String mEncodingFormat = "base64";

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aIS
	 */
	public ChunkableInputStream(String aNS, String aType, InputStream aIS) {
		super(aNS, aType);
		mIS = aIS;
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aIS
	 * @param aZipCompression
	 */
	public ChunkableInputStream(String aNS, String aType, InputStream aIS, boolean aZipCompression) {
		this(aNS, aType, aIS);

		if (aZipCompression) {
			mEncodingFormat = "zipBase64";
		}
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aIS
	 * @param aEncodingFormat
	 */
	public ChunkableInputStream(String aNS, String aType, InputStream aIS, String aEncodingFormat) {
		this(aNS, aType, aIS);

		mEncodingFormat = aEncodingFormat;
	}

	/**
	 *
	 * @return
	 */
	public InputStream getIS() {
		return mIS;
	}

	@Override
	public Iterator<Token> getChunksIterator() {

		return new Iterator<Token>() {
			@Override
			public boolean hasNext() {
				try {
					return mIS.available() > 0;
				} catch (IOException lEx) {
					return false;
				}
			}

			@Override
			public Token next() {
				try {
					int lLength = (mIS.available() > getFragmentSize()) ? getFragmentSize() : mIS.available();
					Token lChunk = TokenFactory.createToken();
					lChunk.setChunkType("stream" + getUniqueChunkId());

					byte[] lBA = new byte[lLength];
					mIS.read(lBA, 0, lLength);

					String lData = new String(lBA);

					lChunk.setMap("enc", new MapAppender().append("data", mEncodingFormat).getMap());
					lChunk.setString("data", lData);

					return lChunk;
				} catch (Exception lEx) {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported on InputStream objects!");
			}
		};
	}
}