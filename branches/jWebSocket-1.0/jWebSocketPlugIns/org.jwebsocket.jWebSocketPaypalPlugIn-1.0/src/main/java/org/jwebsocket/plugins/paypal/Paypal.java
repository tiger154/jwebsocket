/*
 * Copyright 2014 zcool.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jwebsocket.plugins.paypal;

import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zcool
 */
public class Paypal {
    public Properties getProperties() throws FileNotFoundException, Exception {
        InputStream inputStream = new FileInputStream("/home/zcool/src/jWebSocket/branches/jWebSocket-1.0/jWebSocketPlugIns/org.jwebsocket.jWebSocketPaypalPlugIn-1.0/src/main/resources/sdk_config.properties");
        
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(Paypal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return properties;
    }
    
    
    public String getAccessToken() throws Exception{
        Properties properties = getProperties();
        
        String accessToken = null;
        try {
            accessToken = new OAuthTokenCredential(properties.getProperty("clientID"), properties.getProperty("clientSecret")).getAccessToken();
        } catch (PayPalRESTException ex) {
            Logger.getLogger(Paypal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accessToken;
    }
    
    
    
    
}



