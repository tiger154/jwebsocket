//	---------------------------------------------------------------------------
//	jWebSocket ChunkableInputStream (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
import java.util.LinkedList;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The class implements the IChunkable interface to support the transmission of
 * data from a InputStream to the client
 *
 * @author kyberneees
 */
public class ChunkableInputStream extends BaseChunkable {

	private final InputStream mIS;

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

					LinkedList<Integer> lData = new LinkedList<Integer>();
					while (lLength > 0) {
						lData.add(mIS.read());
						lLength--;
					}

					lChunk.setList("data", lData);
					return lChunk;
				} catch (IOException lEx) {
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