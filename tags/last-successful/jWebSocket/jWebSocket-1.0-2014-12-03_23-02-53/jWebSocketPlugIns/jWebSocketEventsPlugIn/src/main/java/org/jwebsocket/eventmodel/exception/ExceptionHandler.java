//	---------------------------------------------------------------------------
//	jWebSocket - ExceptionHandler (Community Edition, CE)
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
package org.jwebsocket.eventmodel.exception;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import javolution.util.FastSet;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.eventmodel.api.IExceptionNotifier;
import org.jwebsocket.logging.Logging;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * An Exception handler is a component used to handle (uncaught) exceptions
 * during C2S event notification work-flow.
 *
 * @author Rolando Santamaria Maso
 */
public class ExceptionHandler implements IExceptionHandler {

	private static final Logger mLog = Logging.getLogger();
	private Set<IExceptionNotifier> mNotifiers = new FastSet<IExceptionNotifier>();

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * {@inheritDoc }
	 *
	 * @param lEx
	 */
	@Override
	public void process(Exception lEx) {
		String lExMsg = lEx.getClass().getName() + ":" + lEx.getMessage();
		if (mLog.isDebugEnabled() && !lEx.getClass().equals(BadCredentialsException.class)) {
			mLog.error(lExMsg, lEx);
		} else {
			mLog.error(lExMsg);
		}

		//Executing notifications
		if (null != mNotifiers) {
			for (Iterator<IExceptionNotifier> lIt = mNotifiers.iterator(); lIt.hasNext();) {
				IExceptionNotifier lNotifier = lIt.next();
				lNotifier.notify(lEx);
			}
		}
	}

	/**
	 * Execute the <tt>processException</tt> method according to the custom
	 * exception class
	 *
	 * @param aExceptionHandler The IExceptionHandler that will process the
	 * exception
	 * @param aEx The exception to process
	 */
	public static void callProcessException(IExceptionHandler aExceptionHandler, Exception aEx) {
		Class<? extends Exception> aExClass = aEx.getClass();
		Class<? extends IExceptionHandler> aExceptionHandlerClass = aExceptionHandler.getClass();

		try {
			Method aMethod = aExceptionHandlerClass.getMethod("process", aExClass);
			aMethod.invoke(aExceptionHandler, aExClass.cast(aEx));
		} catch (NoSuchMethodException lEx) {
			//Calling the base method
			aExceptionHandler.process(aEx);
		} catch (Exception lEx) {
			mLog.error(lEx.getMessage(), lEx);
		}
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void shutdown() throws Exception {
	}

	/**
	 * @return The collection of exception notifier providers
	 */
	public Set<IExceptionNotifier> getNotifiers() {
		return mNotifiers;
	}

	/**
	 * @param aNotifiers The collection of exception notifiers to set
	 */
	public void setNotifiers(Set<IExceptionNotifier> aNotifiers) {
		this.mNotifiers = aNotifiers;
	}
}
