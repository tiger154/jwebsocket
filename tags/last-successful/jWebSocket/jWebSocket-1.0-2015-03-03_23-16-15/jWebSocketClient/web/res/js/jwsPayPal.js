//	---------------------------------------------------------------------------
//	jWebSocket Remote Shell Demo (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
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

jws.PayPalPlugIn = {
    NS: 'org.jwebsocket.plugins.paypal',
            
    payPalCreatePayment: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_payment";
        
        this.sendToken(aOptions, aCallBacks);
    },
    
    payPalExecutePayment: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_execute";
        
        this.sendToken(aOptions, aCallBacks);
    },
    
    payPalListPayments: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_list";
        
        this.sendToken(aOptions, aCallBacks);
    },
    
    payPalGetPayment: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_get";
        
        this.sendToken(aOptions, aCallBacks);
    },
    
    payPalSale: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_sale";
        
        this.sendToken(aOptions, aCallBacks);
    },
    
    payPalSaleRefund: function(aOptions, aCallBacks) {
        aOptions.ns = jws.PayPalPlugIn.NS;
        aOptions.type = "paypal_refund";
        
        this.sendToken(aOptions, aCallBacks);
    }
};

jws.oop.addPlugIn(jws.jWebSocketTokenClient, jws.PayPalPlugIn);

