//	---------------------------------------------------------------------------
//	jWebSocket - IChunkable (Community Edition, CE)
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
package org.jwebsocket.api;

import java.util.Iterator;
import org.jwebsocket.token.Token;

/**
 * The interface allows to stream the content of big size data structures
 * chunking logically it content instead of use fragmentation. The IChunkable
 * interface is the only way to transmit messages that exceeds the connection
 * MAX_FRAME_SIZE attribute.
 *
 * @author Rolando Santamaria Maso
 */
public interface IChunkable {

	/**
	 * Chunkable name-space attribute is equivalent to Token name-space
	 * attribute.
	 *
	 * @return The chunk name-space
	 */
	String getNS();

	/**
	 * Set the chunkable name-space attribute value
	 *
	 * @param aNS
	 */
	void setNS(String aNS);

	/**
	 * Chunkable type attribute is equivalent to Token type attribute
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
	 * @return The fragment size parameter to be used in the chunks
	 * fragmentation
	 */
	Integer getFragmentSize();

	/**
	 * Set the max fragment size parameter
	 *
	 * @param aFragmentSize
	 */
	void setFragmentSize(Integer aFragmentSize);

	/**
	 *
	 * @return The max fragment size that the chunks can use
	 */
	Integer getMaxFrameSize();

	/**
	 * Set the max fragment size that the chunks can use
	 *
	 * @param aMaxFrameSize
	 */
	void setMaxFrameSize(Integer aMaxFrameSize);

	/**
	 * Allows to iterate over the chunkable object chunks
	 *
	 * @return The chunks iterator
	 */
	Iterator<Token> getChunksIterator();
}
