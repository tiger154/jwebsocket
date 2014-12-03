//	---------------------------------------------------------------------------
//	jWebSocket PayPal Demo (Community Edition, CE)
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
                    ]
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
                        
                        window.top.location.assign(lLink.href);
                    }
                });
            }); 
        }
    });
    
})