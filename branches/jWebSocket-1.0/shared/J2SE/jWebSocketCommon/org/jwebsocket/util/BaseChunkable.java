//	---------------------------------------------------------------------------
//	jWebSocket BaseChunkable (Community Edition, CE)
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

import org.jwebsocket.api.IChunkable;

/**
 * The class suppose to be extended by concrete IChunkable implementations
 *
 * @author Rolando Santamaria Maso
 */
abstract public class BaseChunkable implements IChunkable {

	private String mNS;
	private String mType;
	private Integer mFragmentSize = 1024; // 1 KB by default
	private Integer mMaxFrameSize = -1;
	private Long mUCID = new Long(0);

	/**
	 *
	 * @return
	 */
	public Long getUniqueChunkId() {
		return mUCID++;
	}

	/**
	 *
	 * @param aNS
	 * @param aType
	 */
	public BaseChunkable(String aNS, String aType) {
		mNS = aNS;
		mType = aType;
	}

	@Override
	public String getNS() {
		return mNS;
	}

	@Override
	public void setNS(String aNS) {
		mNS = aNS;
	}

	@Override
	public String getType() {
		return mType;
	}

	@Override
	public void setType(String aType) {
		mType = aType;
	}

	@Override
	public void setFragmentSize(Integer aFragmentSize) {
		mFragmentSize = aFragmentSize;
	}

	@Override
	public Integer getFragmentSize() {
		return mFragmentSize;
	}

	@Override
	public Integer getMaxFrameSize() {
		return mMaxFrameSize;
	}

	@Override
	public void setMaxFrameSize(Integer aMaxFrameSize) {
		mMaxFrameSize = aMaxFrameSize;
	}
}
