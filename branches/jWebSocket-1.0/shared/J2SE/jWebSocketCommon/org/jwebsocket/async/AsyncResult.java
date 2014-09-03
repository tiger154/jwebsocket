//	---------------------------------------------------------------------------
//	jWebSocket AsyncResult (Community Edition, CE)
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
package org.jwebsocket.async;

/**
 *
 * @author Rolando Santamaria Maso
 * @param <T>
 */
public class AsyncResult<T> {

	private T mResult;
	private Throwable mFailure;
	private final AsyncResultHandler<T> mHandler;

	public AsyncResult(AsyncResultHandler<T> aHandler) {
		this.mHandler = aHandler;
	}

	public T getResult() {
		return mResult;
	}

	public Throwable getFailure() {
		return mFailure;
	}

	public void setResult(T aResult) {
		mResult = aResult;

		mHandler.handle(this);
	}

	public void setFailure(Throwable lEx) {
		mFailure = lEx;

		mHandler.handle(this);
	}

	public boolean isSuccees() {
		return mFailure == null;
	}
}
