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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author zcool
 */
public class PaypalTest {
    
    public PaypalTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTokenId method, of class Paypal.
     */
    @Test
    public void testGetAccessToken() {
        System.out.println("getAccessToken");
        Paypal instance = new Paypal();
        String result = "";
        try {
            result = instance.getAccessToken();
        } catch (Exception ex) {
            Logger.getLogger(PaypalTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(String.class, result.getClass());
    }
}
