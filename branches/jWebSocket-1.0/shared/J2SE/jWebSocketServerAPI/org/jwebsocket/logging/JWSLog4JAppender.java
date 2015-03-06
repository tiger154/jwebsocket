//	---------------------------------------------------------------------------
//	jWebSocket - Log4J Appender Support (Community Edition, CE)
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
package org.jwebsocket.logging;

import javolution.util.FastSet;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Alexander Schulze
 */
public class JWSLog4JAppender extends AppenderSkeleton {

	private static final FastSet<ILog4JAppender> mAppenders = new FastSet<ILog4JAppender>();
	private static JWSLog4JAppender mInstance = null;

	/**
	 *
	 */
	public JWSLog4JAppender() {
		mInstance = this;
	}

	/**
	 *
	 * @param aLE
	 */
	@Override
	protected void append(LoggingEvent aLE) {
		synchronized (this) {
			for (ILog4JAppender lAppender : mAppenders) {
				lAppender.append(aLE);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void close() {
		// nothing to do here
	}

	/**
	 *
	 * @return
	 */
	@Override
	public boolean requiresLayout() {
		// the layout is done by the appenders themselves
		return false;
	}

	/**
	 *
	 * @return
	 */
	public static JWSLog4JAppender getInstance() {
		return mInstance;
	}

	/**
	 *
	 * @param aAppender
	 */
	public void addAppender(ILog4JAppender aAppender) {
		synchronized (this) {
			mAppenders.add(aAppender);
		}
	}

	/**
	 *
	 * @param aAppender
	 */
	public void removeAppender(ILog4JAppender aAppender) {
		synchronized (this) {
			mAppenders.remove(aAppender);
		}
	}

	@Override
	public void finalize() {
		super.finalize();
		mInstance = null;
	}

}
