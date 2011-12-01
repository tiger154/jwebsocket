//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Native SQL Access for JDBC Plug-In
//  Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.spring;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;

/**
 *
 * @author alexanderschulze
 */
public class ServerXmlBeanFactory extends XmlBeanFactory {

    
    
	/**
	 * 
	 * @param aRes
	 */
	public ServerXmlBeanFactory(Resource aRes) {
		super(aRes);
	}

	/**
	 * 
	 * @param aRes
	 * @param aBeanClassLoader
	 */
	public ServerXmlBeanFactory(Resource aRes, ClassLoader aBeanClassLoader) {
		super(aRes);
		super.setBeanClassLoader(aBeanClassLoader);
	}

	/**
	 * 
	 * @param aBeanClassLoader
	 */
	@Override
	public void setBeanClassLoader(ClassLoader aBeanClassLoader) {
		super.setBeanClassLoader(aBeanClassLoader);
	}
}
