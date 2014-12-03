//	---------------------------------------------------------------------------
//	jWebSocket - Settings for SMS Plug-in (Community Edition, CE)
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
package org.jwebsocket.plugins.sms;

import org.jwebsocket.plugins.itemstorage.api.IItemDefinition;

/**
 * Contains the SMS plug-in settings.
 *
 * @author Alexander Schulze
 */
public class Settings {

	private ISMSProvider mProvider;
	private String mCollectionName,
			mCollectionAccessPassword,
			mCollectionSecretPassword;
	private IItemDefinition mSMSItemDefinition;

	/**
	 * Return the SMS provider instance defined.
	 *
	 * @return the SMS provider
	 */
	public ISMSProvider getProvider() {
		return mProvider;
	}

	/**
	 * Set the SMS provider instance to use.
	 *
	 * @param aProvider an instance of the SMS provider
	 */
	public void setProvider(ISMSProvider aProvider) {
		mProvider = aProvider;
	}

	/**
	 * Get the SMS storage collection name
	 *
	 * @return
	 */
	public String getCollectionName() {
		return mCollectionName;
	}

	/**
	 * Set the SMS storage collection name
	 *
	 * @param aCollectionName
	 */
	public void setCollectionName(String aCollectionName) {
		mCollectionName = aCollectionName;
	}

	/**
	 * Get the SMS storage collection access password
	 *
	 * @return
	 */
	public String getCollectionAccessPassword() {
		return mCollectionAccessPassword;
	}

	/**
	 * Set the SMS storage collection access password
	 *
	 * @param aCollectionAccessPassword
	 */
	public void setCollectionAccessPassword(String aCollectionAccessPassword) {
		mCollectionAccessPassword = aCollectionAccessPassword;
	}

	/**
	 * Get the SMS storage collection type
	 *
	 * @return
	 */
	public String getCollectionType() {
		return getSMSItemDefinition().getType();
	}

	/**
	 * Get the SMS storage collection secret password
	 *
	 * @return
	 */
	public String getCollectionSecretPassword() {
		return mCollectionSecretPassword;
	}

	/**
	 * Set the SMS storage collection secret password
	 *
	 * @param aCollectionSecretPassword
	 */
	public void setCollectionSecretPassword(String aCollectionSecretPassword) {
		mCollectionSecretPassword = aCollectionSecretPassword;
	}

	/**
	 * Get the SMS item definition for item storage
	 *
	 * @return
	 */
	public IItemDefinition getSMSItemDefinition() {
		return mSMSItemDefinition;
	}

	/**
	 * Set the SMS item definition for item storage
	 *
	 * @param aSMSItemDefinition
	 */
	public void setSMSItemDefinition(IItemDefinition aSMSItemDefinition) {
		mSMSItemDefinition = aSMSItemDefinition;
	}

}
