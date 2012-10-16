//  ---------------------------------------------------------------------------
//  jWebSocket - IChunkable
//  Copyright (c) 2012 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.api;

import java.util.Iterator;
import org.jwebsocket.token.Token;

/**
 * The interface allows to stream the content of big size data structures
 * chunking logically it content instead of use fragmentation. The IChunkable
 * interface is the only way to transmit messages that exceeds the connection
 * MAX_FRAME_SIZE attribute.
 *
 * @author kyberneees
 */
public interface IChunkable {

	/**
	 * Chunk name-space attribute is equivalent to Token name-space attribute.
	 *
	 * @return The chunk name-space
	 */
	String getNS();

	/**
	 * Set the chunk name-space attribute value
	 *
	 * @param aNS
	 */
	void setNS(String aNS);

	/**
	 * Chunk type attribute is equivalent to Token type attribute
	 *
	 * @return The chunk type
	 */
	String getType();

	/**
	 * Set the type attribute value
	 *
	 * @param aType
	 */
	void setType(String aType);

	/**
	 *
	 * @return The max fragment size that the chunks can use
	 */
	Integer getFragmentSize();

	/**
	 * Set the max fragment size that the chunks can use
	 *
	 * @param aFragmentSize
	 */
	void setFragmentSize(Integer aFragmentSize);

	/**
	 * Allows to iterate over the data structure chunks
	 *
	 * @return The chunks iterator
	 */
	Iterator<Token> getChunksIterator();
}
