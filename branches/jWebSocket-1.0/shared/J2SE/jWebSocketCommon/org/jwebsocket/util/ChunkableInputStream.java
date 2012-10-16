//	---------------------------------------------------------------------------
//	jWebSocket - ChunkableInputStream
//	Copyright (c) 2012 Innotrade GmbH
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

	public ChunkableInputStream(String aNS, String aType, InputStream aIS) {
		super(aNS, aType);
		mIS = aIS;
	}

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
					lChunk.setChunkType("stream");

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