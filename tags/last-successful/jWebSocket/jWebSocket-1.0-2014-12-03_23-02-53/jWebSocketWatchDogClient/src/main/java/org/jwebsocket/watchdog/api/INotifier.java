// ---------------------------------------------------------------------------
// jWebSocket - < Description/Name of the Module >
// Copyright(c) 2010-212 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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
package org.jwebsocket.watchdog.api;

import java.util.List;
import org.jwebsocket.api.IInitializable;

/**
 *
 * @author Lester Alfonso Zaila Viejo(telnet_1, UCI, Artemisa)
 */
public interface INotifier extends IInitializable {

    /**
     * Get the ID of the notifier
     */
    public String getId();

    /*
     * set the ID of the notifier
     */
    public void setId(String id);

    /*
     * get the Decription of the notifier
     */
    public String getDescription();

    /*
     * set the Decription of the notifier
     */
    public void setDescription(String description);

    /*
     * List of users whom will receive the notifications
     */
    List<String> getTo();

    /*
     * set the users whom will receive the notifications
     */
    void setTo(List<String> to);

    /*
     * sending the message
     */
    public void notify(String message);
}
