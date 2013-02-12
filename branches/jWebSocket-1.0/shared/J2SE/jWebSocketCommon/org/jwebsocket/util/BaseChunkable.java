//	---------------------------------------------------------------------------
//	jWebSocket - BaseChunkable
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

import org.jwebsocket.api.IChunkable;

/**
 * The class suppose to be extended by concrete IChunkable implementations
 *
 * @author kyberneees
 */
abstract class BaseChunkable implements IChunkable {

	private String mNS;
	private String mType;
	private Integer mFragmentSize = 1024; // 1 KB by default
	private Integer mMaxFrameSize = -1;
	private Long mUCID = new Long(0);

	public Long getUniqueChunkId() {
		return mUCID++;
	}

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
