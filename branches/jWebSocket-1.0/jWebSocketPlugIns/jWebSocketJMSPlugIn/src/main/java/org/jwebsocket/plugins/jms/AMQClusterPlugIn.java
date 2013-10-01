//  ---------------------------------------------------------------------------
//  jWebSocket - AMQClusterPlugIn (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2013 Innotrade GmbH (jWebSocket.org)
//  Alexander Schulze, Germany (NRW)
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
package org.jwebsocket.plugins.jms;

import java.util.List;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

/**
 *
 * @author kyberneees
 */
public class AMQClusterPlugIn implements BrokerPlugin {

	private List<String> mTargetDestinations;
	private String username, password;

	@Override
	public Broker installPlugin(Broker broker) throws Exception {
		return new AMQClusterFilter(broker, mTargetDestinations, username, password);
	}

	public List<String> getTargetDestinations() {
		return mTargetDestinations;
	}

	public void setTargetDestinations(List<String> aTargetDestinations) {
		mTargetDestinations = aTargetDestinations;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String aUsername) {
		this.username = aUsername;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String aPassword) {
		this.password = aPassword;
	}
}
