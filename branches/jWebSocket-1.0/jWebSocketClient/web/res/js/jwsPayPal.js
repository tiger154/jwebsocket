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

