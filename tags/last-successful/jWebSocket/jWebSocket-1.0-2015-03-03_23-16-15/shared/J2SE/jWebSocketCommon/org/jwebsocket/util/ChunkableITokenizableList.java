//	---------------------------------------------------------------------------
//	jWebSocket ChunkableITokenizableList (Community Edition, CE)
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jwebsocket.api.ITokenizable;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * The class implements the IChunkable interface to support the transmission of ITokenizable lists.
 *
 * @author Rolando Santamaria Maso
 */
public class ChunkableITokenizableList extends BaseChunkable {

	private final List<ITokenizable> mList;
	private final int mMaxSerializedItemSize;

	/**
	 *
	 * @param aNS
	 * @param aType
	 * @param aList The list containing the ITokenizable objects.
	 * @param aMaxSerializedItemSize The maximum number of bytes that each ITokenizable object may
	 * consume once serialized.
	 */
	public ChunkableITokenizableList(String aNS, String aType, List<ITokenizable> aList,
			int aMaxSerializedItemSize) {
		super(aNS, aType);
		mList = aList;
		mMaxSerializedItemSize = aMaxSerializedItemSize;
	}

	/**
	 *
	 * @return
	 */
	public List<ITokenizable> getList() {
		return mList;
	}

	@Override
	public Iterator<Token> getChunksIterator() {
		final Integer lItemsPerChunk = getMaxFrameSize() / mMaxSerializedItemSize;
		if (lItemsPerChunk < 1) {
			throw new RuntimeException("The 'max item size' value exceeds the chunk 'max frame size' restriction!");
		}

		return new Iterator<Token>() {
			private Integer mPosition = 0;

			@Override
			public boolean hasNext() {
				return mPosition < mList.size();
			}

			@Override
			public Token next() {
				try {
					List<ITokenizable> lItemsToBeSent = new ArrayList<ITokenizable>(lItemsPerChunk);
					Token lChunk = TokenFactory.createToken();

					Integer lRound = 0;
					while (mPosition < mList.size() && lRound < lItemsPerChunk) {
						lItemsToBeSent.add(mList.get(mPosition));
						mPosition++;
						lRound++;
					}

					lChunk.setChunkType("stream");
					lChunk.setInteger("listSize", mList.size());
					lChunk.setInteger("itemsInRound", lRound);
					lChunk.setInteger("position", mPosition);
					lChunk.setList("data", lItemsToBeSent);

					return lChunk;
				} catch (Exception lEx) {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported!");
			}
		};
	}
}