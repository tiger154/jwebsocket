//	---------------------------------------------------------------------------
//	jWebSocket - StaticUserDetails (Community Edition, CE)
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
package org.jwebsocket.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * jWebSocket static user details service for jWebSocket.xml user's information.
 *
 * @author kyberneees
 */
public class StaticUserDetails implements UserDetailsService {

	/**
	 *
	 * @param aUsername
	 * @return
	 * @throws UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserByUsername(String aUsername) throws UsernameNotFoundException {
		if (!SecurityFactory.isValidUser(aUsername)) {
			throw new UsernameNotFoundException(aUsername);
		}

		User lUser = SecurityFactory.getUser(aUsername);
		List<GrantedAuthority> lAuthorities = new ArrayList<GrantedAuthority>();
		Rights lRights = SecurityFactory.getUserRights(aUsername);

		for (Right lR : lRights.getRights()) {
			lAuthorities.add(new SimpleGrantedAuthority(lR.getId()));
		}

		return new org.springframework.security.core.userdetails.User(aUsername, lUser.getPassword(), lAuthorities);
	}
}
