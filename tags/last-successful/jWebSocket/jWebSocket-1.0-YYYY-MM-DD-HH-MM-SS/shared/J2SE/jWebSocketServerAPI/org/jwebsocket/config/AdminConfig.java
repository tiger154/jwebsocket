// ---------------------------------------------------------------------------
// jWebSocket - AdminConfig (Community Edition, CE)
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
package org.jwebsocket.config;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.config.xml.FilterConfig;
import org.jwebsocket.config.xml.PluginConfig;

/**
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class AdminConfig implements Config {

	private List<PluginConfig> mPlugins;
	private List<FilterConfig> mFilters;

	/**
	 *
	 */
	public AdminConfig() {
		mPlugins = new FastList<PluginConfig>();
		mFilters = new FastList<FilterConfig>();
	}

	@Override
	public void validate() {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 *
	 * @return
	 */
	public List<FilterConfig> getFilters() {
		return mFilters;
	}

	/**
	 *
	 * @param aFilters
	 */
	public void setFilters(List<FilterConfig> aFilters) {
		this.mFilters = aFilters;
	}

	/**
	 *
	 * @return
	 */
	public List<PluginConfig> getPlugins() {
		return mPlugins;
	}

	/**
	 *
	 * @param aPlugins
	 */
	public void setPlugins(List<PluginConfig> aPlugins) {
		this.mPlugins = aPlugins;
	}

	/**
	 *
	 * @param aIdPlugIn
	 * @return
	 */
	public PluginConfig getPlugin(String aIdPlugIn) {
		if (mPlugins != null) {
			for (int i = 0; i < mPlugins.size(); i++) {
				if (mPlugins.get(i).getId().equals(aIdPlugIn)) {
					return mPlugins.get(i);
				}
			}
		}
		return null;
	}

	/**
	 *
	 * @param aIdFilter
	 * @return
	 */
	public FilterConfig getFilter(String aIdFilter) {
		if (mFilters != null) {
			for (int i = 0; i < mFilters.size(); i++) {
				if (mFilters.get(i).getId().equals(aIdFilter)) {
					return mFilters.get(i);
				}
			}
		}
		return null;
	}
}
