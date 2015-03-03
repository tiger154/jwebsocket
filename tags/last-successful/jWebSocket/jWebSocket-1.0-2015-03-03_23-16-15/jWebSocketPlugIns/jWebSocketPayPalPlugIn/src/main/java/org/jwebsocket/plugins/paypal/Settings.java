//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket PayPal Plug-In (Community Edition, CE)
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
package org.jwebsocket.plugins.paypal;

/**
 *
 * @author Omar Antonio Díaz Peña
 */
public class Settings {

    private String mClientID;
    private String mClientSecret;
    private String mReturnUrl;
    private String mCancelUrl;

    /**
     * Default constructor
     *
     * @param aClientID
     * @param aClientSecret
     * @param aReturnUrl
     * @param aCancelUrl
     */
    public Settings(String aClientID, String aClientSecret, String aReturnUrl, String aCancelUrl) {
        mClientID = aClientID;
        mClientSecret = aClientSecret;
        mReturnUrl = aReturnUrl;
        mCancelUrl = aCancelUrl;
    }

    /**
     * Client secret 
     * 
     * @return String
     */
    public String getClientSecret() {
        return mClientSecret;
    }

    /**
     * setClientSecret
     * 
     * @param mClientSecret 
     */
    public void setClientSecret(String mClientSecret) {
        this.mClientSecret = mClientSecret;
    }

    /**
     * Return URL
     * 
     * @return String 
     */
    public String getReturnUrl() {
        return mReturnUrl;
    }

    /**
     * Set return URL
     * 
     * @param mReturnUrl 
     */
    public void setReturnUrl(String mReturnUrl) {
        this.mReturnUrl = mReturnUrl;
    }

    /**
     * Empty
     */
    public Settings() {
    }

    /**
     * Client ID
     * 
     * @return String
     */
    public String getClientID() {
        return mClientID;
    }

    /**
     * Set client ID
     * 
     * @param aClientID
     */
    public void setClientID(String aClientID) {
        mClientID = aClientID;
    }

    /**
     * Client URL 
     * 
     * @return String
     */
    public String getCancelUrl() {
        return mCancelUrl;
    }

    /**
     * Set cancel URL
     * 
     * @param mCancelUrl 
     */
    public void setCancelUrl(String mCancelUrl) {
        this.mCancelUrl = mCancelUrl;
    }
    
    
}
