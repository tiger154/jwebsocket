//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
 * @author kyberneees
 */
public class ExceptionHandler implements IExceptionHandler {

	private static Logger mLog = Logging.getLogger();
	private Set<IExceptionNotifier> mNotifiers = new FastSet<IExceptionNotifier>();

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * {@inheritDoc }
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
