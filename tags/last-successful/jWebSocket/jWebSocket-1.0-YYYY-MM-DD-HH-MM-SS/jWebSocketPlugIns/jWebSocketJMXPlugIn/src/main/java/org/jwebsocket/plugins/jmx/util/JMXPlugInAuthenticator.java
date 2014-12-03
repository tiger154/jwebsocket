// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugInAuthenticator (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jmx.util;

import java.util.Collection;
import java.util.Collections;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import org.jwebsocket.spring.JWebSocketBeanFactory;
import org.jwebsocket.util.Tools;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.memory.InMemoryDaoImpl;

/**
 * Class that implements the security mechanism for the remote access to the
 * module via the RMI protocol.
 *
 * @author Lisdey Perez Hernandez
 */
public class JMXPlugInAuthenticator implements JMXAuthenticator {

	private static String mConfigPath;

	/**
	 * The class default constructor.
	 */
	public JMXPlugInAuthenticator() {
	}
	/*
	 * {@inheritDoc} 
	 */

	@Override
	public Subject authenticate(Object aCredentials) {
		if (!(aCredentials instanceof String[])) {
			if (aCredentials == null) {
				throw new SecurityException("Credentials required");
			}
			throw new SecurityException("Credentials should be String[]");
		}

		final String[] lCredentials = (String[]) aCredentials;
		if (lCredentials.length != 2) {
			throw new SecurityException("Credentials should have 2 elements");
		}

		// Perform authentication
		String lUserName = lCredentials[0];
		String lPassword = lCredentials[1];

		// Load config file to perform authentication
		JWebSocketBeanFactory.load(mConfigPath, getClass().getClassLoader());
		ApplicationContext lFactory = JWebSocketBeanFactory.getInstance();
		/*
		 * Resource lResource = new FileSystemResource(mConfigPath);
		 * XmlBeanFactory lFactory = new ServerXmlBeanFactory(lResource,
		 * getClass().getClassLoader());
		 */
		InMemoryDaoImpl lAuthentication = (InMemoryDaoImpl) lFactory.getBean("staticAuthUserDetailsService");
		UserDetails lUser = lAuthentication.loadUserByUsername(lUserName);

		if (lUser != null) {
			if (lUser.isEnabled()) {
				if (lUser.getPassword().equals(Tools.getMD5(lPassword))) {
					if (credentialAllowed((Collection<GrantedAuthority>) lUser.getAuthorities())) {
						return new Subject(true,
								Collections.singleton(new JMXPrincipal(lUserName)),
								Collections.EMPTY_SET,
								Collections.EMPTY_SET);
					} else {
						throw new SecurityException("Invalid credentials");
					}
				} else {
					throw new SecurityException("Invalid password");
				}
			} else {
				throw new SecurityException("User disabled");
			}
		} else {
			throw new SecurityException("Invalid user.");
		}
	}

	/**
	 *
	 * @param aConfigPath
	 */
	public static void setConfigPath(String aConfigPath) {
		JMXPlugInAuthenticator.mConfigPath = aConfigPath;
	}

	private Boolean credentialAllowed(Collection<? extends GrantedAuthority> aCredentials) {
		for (GrantedAuthority lCredential : aCredentials) {
			if (lCredential.getAuthority().equals("ROLE_ADMIN_JMX")) {
				return true;
			}
		}
		return false;
	}
}