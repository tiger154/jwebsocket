// ---------------------------------------------------------------------------
// jWebSocket - < INotifier >
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
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
     *
     * @return
     */
    public String getId();

    /**
     * set the ID of the notifier
     *
     * @param id
     */
    public void setId(String id);

    /**
     * Get the description of the notifier
     *
     * @return
     */
    public String getDescription();

    /**
     * set the description of the notifier
     *
     * @param description
     */
    public void setDescription(String description);

    /**
     * List of users whom will receive the notifications
     *
     * @return
     */
    List<String> getTo();

    /**
     * set the users whom will receive the notifications
     *
     * @param to
     */
    void setTo(List<String> to);

    /**
     * sending the message
     *
     * @param message
     */
    public void notify(String message);
}
