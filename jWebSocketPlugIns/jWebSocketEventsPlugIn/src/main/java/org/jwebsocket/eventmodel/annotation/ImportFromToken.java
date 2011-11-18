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
package org.jwebsocket.eventmodel.annotation;

import java.lang.annotation.*;

/**
 * This annotation allows automatic population in custom events fields. Copy 
 * or move value from the incoming tokens to the targeted fields in events.
 *
 * @author kyberneees
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ImportFromToken {

	/**
	 * Allowed strategies: "copy" | "move"
	 *
	 * copy: Keeps the original parameter in the incoming token
	 * move: Remove the original parameter from the incoming token
	 *
	 * @return The importing strategy
	 */
	String strategy() default "move";

	/**
	 * @return The key of the incoming parameter to import. By default
	 * use the same field name as the parameter name.
	 */
	String key() default "";
}
