// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 jwebsocket.org
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
// more details.
// You should have received a copy of the GNU Lesser General Public License along
// with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
package org.jwebsocket.netty.connectors;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jwebsocket.async.IOFutureListener;

/**
 * Netty channel future listener implementation to support NIO listener
 * implementation
 * @author puran
 * @version $Id: NIOInternalFutureListener.java 1051 2010-09-20 05:09:05Z mailtopuran@gmail.com $
 */
public class NIOInternalFutureListener implements ChannelFutureListener {
  
  private IOFutureListener listener = null;
  private NIOFuture source = null;
  
  public NIOInternalFutureListener(NIOFuture theSource, IOFutureListener theListener) {
    this.listener = theListener;
    this.source = theSource;
  }
  
  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    listener.operationComplete(source);
  }
}
