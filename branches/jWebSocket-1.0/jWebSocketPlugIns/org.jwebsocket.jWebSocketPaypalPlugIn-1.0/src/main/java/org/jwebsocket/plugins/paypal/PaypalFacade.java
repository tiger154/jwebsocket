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

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.token.Token;

/**
 *
 * @author zcool
 */
public class PaypalFacade {
    private String clientID;
    private String clientSecret;

    public PaypalFacade(String aClientID, String aClientSecret) {
        this.clientID = aClientID;
        this.clientSecret = aClientSecret;
    }
    
    public String getAccessToken() throws Exception{
        
        String accessToken = null;
        try {
            accessToken = new OAuthTokenCredential(this.clientID, this.clientSecret).getAccessToken();
        } catch (PayPalRESTException ex) {
            Logger.getLogger(PaypalFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accessToken;
    }

    public Payment createPayment(Token aToken) throws Exception {
        Payment lPayment = new Payment();
        lPayment.setIntent(aToken.getString("intent"));
        
        Payer lPayer = new Payer();
        Map lPayerData = (Map)aToken.getMap("payer").get("payer_info");
        
        PayerInfo lPayerInfo = new PayerInfo();
        lPayer.setPayerInfo(lPayerInfo);
        lPayer.setPaymentMethod("paypal");
        lPayerInfo.setFirstName((String) lPayerData.get("first_name"));
        lPayerInfo.setLastName((String) lPayerData.get("last_name"));
        lPayerInfo.setPayerId((String) lPayerData.get("payer_id"));
        lPayer.setPayerInfo(lPayerInfo);
        Map lRedirectData = (Map)aToken.getMap("redirect_urls");
        
        RedirectUrls lRedirectUrls = new RedirectUrls();
        lRedirectUrls.setReturnUrl((String)lRedirectData.get("return_url"));
        lRedirectUrls.setCancelUrl((String)lRedirectData.get("cancel_url"));
        lPayment.setRedirectUrls(lRedirectUrls);
        
        lPayment.setPayer(lPayer);
        List lTransactionsData = aToken.getList("transactions");

        LinkedList<Transaction> lTransactions = new LinkedList<Transaction>();
        
        for (Object object : lTransactionsData) {
            Map lTransactionData = (Map)object;
            Map lTmpListData = (Map)lTransactionData.get("item_list");
            List lItemListData = (List)lTmpListData.get("items");
            Map lAmountData = (Map)lTransactionData.get("amount");
            Transaction lTransaction = new Transaction();
            
            ItemList lItemList = new ItemList();
            List<Item> lTmpItemList = new ArrayList<Item>();
            
            for (Object lItemData : lItemListData) {
                Map lTmpItemData = (Map)lItemData;
                Item lItem = new Item();
                lItem.setCurrency((String)lTmpItemData.get("currency"));
                lItem.setPrice((String)lTmpItemData.get("price"));
                lItem.setName((String)lTmpItemData.get("name"));
                lItem.setQuantity(lTmpItemData.get("quantity").toString());
                lItem.setSku((String)lTmpItemData.get("sku"));
                
                lTmpItemList.add(lItem);
            }
            
            lItemList.setItems(lTmpItemList);
            lTransaction.setItemList(lItemList);
            
            Amount lAmount = new Amount();
            lAmount.setCurrency((String)lAmountData.get("currency"));
            lAmount.setTotal((String)lAmountData.get("total"));
            lTransaction.setAmount(lAmount);
            lTransaction.setDescription(
                    (String)lTransactionData.get("description")
            );
            lTransactions.add(lTransaction);
        }
        
        lPayment.setTransactions(lTransactions);
        
        return lPayment.create(this.getAccessToken());
    }
}



