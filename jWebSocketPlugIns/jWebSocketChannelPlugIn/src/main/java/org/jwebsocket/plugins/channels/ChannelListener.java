//  ---------------------------------------------------------------------------
//  jWebSocket - ChannelListener
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

import org.jwebsocket.token.Token;

/**
 * Listener interface for the <tt>Channel</tt>. 
 * @author puran, aschulze, kyberneees
 * @version $Id: ChannelListener.java 1592 2011-02-20 00:49:48Z fivefeetfurther $
 */
public interface ChannelListener {

	/**
	 * Called when the channel is initialized.
	 * @param aChannel the started channel
	 */
	void channelInitialized(Channel aChannel);
	
	/**
	 * Called when the channel is started.
	 * @param aChannel the started channel
	 */
	void channelStarted(Channel aChannel, String aUser);

	/**
	 * called when the channel is stopped for receiving/sending any data
	 * @param aChannel the stopped channel object
	 * @param aUser the user that stopped the channel
	 */
	void channelStopped(Channel aChannel, String aUser);

	/**
	 * called when the is removed
	 * @param aChannel the suspended channel
	 */
	void channelRemoved(Channel aChannel, String aUser);

	/**
	 * called when a new subscriber subscribes the channel
	 * @param aChannel the channel
	 * @param aSubscriber the subscriber
	 */
	void subscribed(Channel aChannel, String aSubscriber);

	/**
	 * called when someone unsuscribes the channel
	 * @param aChannel the channel
	 * @param aSubscriber the subscriber who unsuscribed
	 */
	void unsubscribed(Channel aChannel, String aSubscriber);

	/**
	 * called when channel receives the data from the publisher.
	 * @param aChannel the channel object
	 * @param aToken the token data received that will be broadcasted
	 * to the subscribers
	 */
	void dataReceived(Channel aChannel, Token aToken);

	/**
	 * Called when channel is done with the broadcast of the data it received to 
	 * all the subscribers.
	 * @param aChannel the channel object
	 * @param aToken the token data which was broadcasted
	 */
	void dataBroadcasted(Channel aChannel, Token aToken);
}
