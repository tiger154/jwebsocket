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
import java.util.Set;
import org.jwebsocket.eventmodel.api.IExceptionHandler;
import org.jwebsocket.logging.Logging;
import org.apache.log4j.Logger;
import org.jwebsocket.eventmodel.api.IExceptionNotifierProvider;

/**
 * An Exception handler is a component used to handle (uncaught) exceptions in a 
 * work-flow. 
 *
 * @author kyberneees
 */
public class ExceptionHandler implements IExceptionHandler {

	private static Logger mLog = Logging.getLogger(ExceptionHandler.class);
	private Set<IExceptionNotifierProvider> notifierProviders;

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
	public void process(Exception ex) {
		if (mLog.isDebugEnabled()) {
			mLog.error(ex.toString(), ex);
		} else {
			mLog.error(ex.getMessage());
		}

		//Executing notifications
		if (null != notifierProviders) {
			for (IExceptionNotifierProvider p : notifierProviders) {
				p.notify(ex);
			}
		}
	}

	/**
	 * Execute the <tt>processException</tt> method according to the custom exception class
	 * 
	 * @param aExceptionHandler The IExceptionHandler that will process the exception 
	 * @param aEx The exception to process
	 */
	public static void callProcessException(IExceptionHandler aExceptionHandler, Exception aEx) {
		Class<? extends Exception> aExClass = aEx.getClass();
		Class<? extends IExceptionHandler> aExceptionHandlerClass = aExceptionHandler.getClass();

		try {
			Method aMethod = aExceptionHandlerClass.getMethod("process", aExClass);
			aMethod.invoke(aExceptionHandler, aExClass.cast(aEx));
		} catch (NoSuchMethodException ex) {
			//Calling the base method
			aExceptionHandler.process(aEx);
		} catch (Exception ex) {
			mLog.error(ex.getMessage(), ex);
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
	public Set<IExceptionNotifierProvider> getNotifierProviders() {
		return notifierProviders;
	}

	/**
	 * @param notifierProvidersThe collection of exception notifier providers to set
	 */
	public void setNotifierProviders(Set<IExceptionNotifierProvider> notifierProviders) {
		this.notifierProviders = notifierProviders;
	}
}
