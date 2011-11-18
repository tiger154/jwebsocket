//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelLifeCycle
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
package org.jwebsocket.plugins.channels;

/**
 * Lifecycle interface for a channel
 * 
 * @author puran
 * @version $Id: ChannelLifeCycle.java 1270 2010-12-23 08:53:06Z fivefeetfurther $
 */
public interface ChannelLifeCycle {

	void init();

	void start(String user) throws ChannelLifeCycleException;

	void suspend(String user) throws ChannelLifeCycleException;

	void stop(String user) throws ChannelLifeCycleException;
}
