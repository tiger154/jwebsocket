/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.quota.api;

import org.jwebsocket.token.Token;

/**
 *
 * @author osvaldo
 */
public interface IQuotaSingleInstance {
    
        /**
         * 
         * @return the quota value
         */
	public long getvalue();

        /**
         *  Return the Instance owner of the quota
         *  admin, administrator, sms-app, or x-module
         * @return 
         */
	public String getInstance();
        
        /**
         * 
         * @return the quota unique ID 
         */
	public String getUuid();

        /**
         * 
         * @return the namespace of the feature that the quota is apply to
         */
	public String getNamespace();

        /**
         * 
         * @return the quota type 
         */
	public String getQuotaType();

        /**
         * The type of the Instance 
         * (e.g) user, gruop of users, app or module
         * @return 
         */
	public String getInstanceType();

        public void writeToToken(Token lAuxToken);
}
