//	---------------------------------------------------------------------------
//	jWebSocket JWSTimerTask (Community Edition, CE)
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

import java.util.TimerTask;

/**
 *
 * @author Rolando Santamaria Maso
 */
public class JWSTimerTask extends TimerTask {

	private Runnable mTask;

	public JWSTimerTask() {
	}

	/**
	 * Optionally received the Runnable object that is called in the wrapped run
	 * method.
	 *
	 * @param aTask
	 */
	public JWSTimerTask(Runnable aTask) {
		this.mTask = aTask;
	}

	@Override
	public void run() {
		try {
			runTask();
		} catch (Exception lEx) {

		}
	}

	/**
	 * Replacement for classic run method.Descendant classes will optionally
	 * override this method.
	 */
	public void runTask() {
		if (null != mTask) {
			mTask.run();
		}
	}

}
