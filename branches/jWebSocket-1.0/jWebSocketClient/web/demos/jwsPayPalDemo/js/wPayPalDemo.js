$(function() {
    
    $("#demo_box").auth({
        OnWelcome: function(aToken) {
            $("#btnPay").on("click", function(aEvent){
                mWSC.payPalCreatePayment({
                    intent: 'sale',
                    payer: {
                        payment_method: 'paypal',
                        payer_info: {
                            first_name: $('#name').val(),
                            last_name: $('#last_name').val(),
                            tax_id_type: '1BR_CPF'
                        },
                        status: 'VERIFIED'
                    },
                    transactions: [
                        {
                            item_list: {
                                items: [{
                                    "name": $('#item_name').val(),
                                    "sku": "item",
                                    "price": $('#price').val(),
                                    "currency": $('#currency').val(),
                                    "quantity": 1
                                }]
                            },
                            amount: {
                                currency: $('#currency').val(),
                                total: $('#price').val()
                            }, 
                            description: 'Transaction test'
                        }
                    ],
                    redirect_urls: {
                        return_url: $('#return_url').val(), //'http://localhost/web/demos/jwsPayPal/public_html/return.html',
                        cancel_url: $('#cancel_url').val() //'http://localhost/web/demos/jwsPayPal/public_html/cancel.html'
                    }
                }, {
                    OnSuccess: function(aToken){
                        var lResponseToken = JSON.parse(aToken.data);
                        var lLinks = lResponseToken.links;
                        
                        $.cookie("payment_id", lResponseToken.id, {path: '/'});;
                        
                        for (var i = 0;i < lLinks.length; i++) {
                            var lLink = lLinks[i];
                            
                            if("approval_url" === lLink.rel) {
                                break;
                            }
                        }
                        
                        window.location.assign(lLink.href);
                    }
                });
            }); 
        }
    });
    
})