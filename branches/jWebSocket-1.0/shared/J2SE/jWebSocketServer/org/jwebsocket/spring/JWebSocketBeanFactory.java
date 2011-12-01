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

import org.jwebsocket.config.JWebSocketConfig;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * This is required to load the bootstrap.xml config file.
 * It provides a shared beanFactory for all plug-ins and this 
 * allows inter-dependencies between the plug-ins and core components.
 * @author alexanderschulze
 */
public class JWebSocketBeanFactory {

    private static GenericApplicationContext mParentContext = null;

    public static GenericApplicationContext getInstance() {
        if (null == mParentContext) {
            mParentContext = new GenericApplicationContext(new DefaultListableBeanFactory());
        }
        return mParentContext;

    }

    public static void load(String aPath, ClassLoader aBeanClassLoader) {
        XmlBeanDefinitionReader lXmlReader = new XmlBeanDefinitionReader(getInstance());
        if (null != aBeanClassLoader) {
            lXmlReader.setBeanClassLoader(aBeanClassLoader);
        }
        lXmlReader.loadBeanDefinitions(new FileSystemResource(aPath));
    }
    /**
     * 
     * @param aBeanClassLoader
     */
    /*
    @Override
    public void setBeanClassLoader(ClassLoader aBeanClassLoader) {
    super.setBeanClassLoader(aBeanClassLoader);
    }
     */
}
