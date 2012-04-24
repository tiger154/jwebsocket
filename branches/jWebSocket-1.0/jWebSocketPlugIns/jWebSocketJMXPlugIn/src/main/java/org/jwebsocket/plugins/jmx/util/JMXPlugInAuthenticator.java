// ---------------------------------------------------------------------------
// jWebSocket - JMXPlugIn v1.0
// Copyright(c) 2010-2012 Innotrade GmbH, Herzogenrath, Germany, jWebSocket.org
// ---------------------------------------------------------------------------
// THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
// THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
// THIS CODE IS NOT YET SECURE AND MAY NOT BE USED FOR PRODUCTION ENVIRONMENTS!
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
 * @author Lisdey Pérez Hernández(lisdey89, UCI)
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
		String lUserName = (String) lCredentials[0];
		String lPassword = (String) lCredentials[1];

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
					if (credentialAllowed((Collection<GrantedAuthority>)lUser.getAuthorities())) {
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