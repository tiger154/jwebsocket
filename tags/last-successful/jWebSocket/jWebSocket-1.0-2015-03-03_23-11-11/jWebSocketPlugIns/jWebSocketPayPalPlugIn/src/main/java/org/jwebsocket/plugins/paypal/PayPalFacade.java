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

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.PaymentHistory;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Refund;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.APIContext;
import com.paypal.core.rest.OAuthTokenCredential;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.token.Token;

/**
 *
 * @author Omar Antonio Díaz Peña
 */
public class PayPalFacade {

    private final String mClientID;
    private final String mClientSecret;

    /**
     * Empty constructor
     * 
     * @param aClientID
     * @param aClientSecret
     */
    public PayPalFacade(String aClientID, String aClientSecret) {
        mClientID = aClientID;
        mClientSecret = aClientSecret;
    }

    /**
     * This method makes a request to the PayPal API and gets back the token for each operation.
     * 
     * @return String 
     * @throws Exception
     */
    public String getAccessToken() throws Exception {
        return new OAuthTokenCredential(mClientID, mClientSecret).getAccessToken();
    }

    /**
     * Creates a Payment.
     * 
     * @param aToken
     * @return Payment
     * @throws Exception
     */
    public Payment createPayment(Token aToken) throws Exception {
        Payment lPayment = new Payment();
        lPayment.setIntent(aToken.getString("intent"));

        Payer lPayer = new Payer();
        Map lPayerData = (Map) aToken.getMap("payer").get("payer_info");

        PayerInfo lPayerInfo = new PayerInfo();
        lPayer.setPayerInfo(lPayerInfo);
        lPayer.setPaymentMethod("paypal");
        lPayerInfo.setFirstName((String) lPayerData.get("first_name"));
        lPayerInfo.setLastName((String) lPayerData.get("last_name"));
        lPayerInfo.setPayerId((String) lPayerData.get("payer_id"));
        lPayer.setPayerInfo(lPayerInfo);

        RedirectUrls lRedirectUrls = new RedirectUrls();
        lRedirectUrls.setReturnUrl(aToken.getString("return_url"));
        lRedirectUrls.setCancelUrl(aToken.getString("cancel_url"));
        lPayment.setRedirectUrls(lRedirectUrls);

        lPayment.setPayer(lPayer);
        List lTransactionsData = aToken.getList("transactions");

        LinkedList<Transaction> lTransactions = new LinkedList<Transaction>();

        for (Object lObject : lTransactionsData) {
            Map lTransactionData = (Map) lObject;
            Map lTmpListData = (Map) lTransactionData.get("item_list");
            List lItemListData = (List) lTmpListData.get("items");
            Map lAmountData = (Map) lTransactionData.get("amount");
            Transaction lTransaction = new Transaction();

            ItemList lItemList = new ItemList();
            List<Item> lTmpItemList = new ArrayList<Item>();

            for (Object lItemData : lItemListData) {
                Map lTmpItemData = (Map) lItemData;
                Item lItem = new Item();
                lItem.setCurrency((String) lTmpItemData.get("currency"));
                lItem.setPrice((String) lTmpItemData.get("price"));
                lItem.setName((String) lTmpItemData.get("name"));
                lItem.setQuantity(lTmpItemData.get("quantity").toString());
                lItem.setSku((String) lTmpItemData.get("sku"));

                lTmpItemList.add(lItem);
            }

            lItemList.setItems(lTmpItemList);
            lTransaction.setItemList(lItemList);

            Amount lAmount = new Amount();
            lAmount.setCurrency((String) lAmountData.get("currency"));
            lAmount.setTotal((String) lAmountData.get("total"));
            lTransaction.setAmount(lAmount);
            lTransaction.setDescription(
                    (String) lTransactionData.get("description")
            );
            lTransactions.add(lTransaction);
        }

        lPayment.setTransactions(lTransactions);

        return lPayment.create(this.getAccessToken());
    }

    /**
     * Executes the payment 
     * 
     * @param aToken
     * @return Payment
     * @throws Exception
     */
    public Payment executePayment(Token aToken) throws Exception {
        Payment lPayment = new Payment();
        lPayment.setId(aToken.getString("payment_id"));

        PaymentExecution lPaymentExecution = new PaymentExecution();
        lPaymentExecution.setPayerId(aToken.getString("payer_id"));

        String lAccessToken = this.getAccessToken();
        APIContext lApiContext = new APIContext(lAccessToken);

        return lPayment.execute(lApiContext, lPaymentExecution);
    }

    /**
     * List payments according to some criteria 
     * 
     * @param aConnector
     * @param aToken
     * @return PaymentHistory
     * @throws Exception
     */
    public PaymentHistory listPayments(WebSocketConnector aConnector, Token aToken) throws Exception {
        HashMap lContainerMap = new HashMap<String, String>();
        Map lPagingData = aToken.getMap("paging");

        lContainerMap.put("start_index", lPagingData.get("start_index").toString());
        lContainerMap.put("count", lPagingData.get("count").toString());
        lContainerMap.put("sort_order", (String) lPagingData.get("sort_order"));

        PaymentHistory lResult = Payment.list(getAccessToken(), lContainerMap);

        return lResult;
    }

    /**
     * Get a Payment 
     * 
     * @param aConnector
     * @param aToken
     * @return Payment
     * @throws Exception
     */
    public Payment getPayment(WebSocketConnector aConnector, Token aToken) throws Exception {
        Payment lResult = Payment.get(getAccessToken(), aToken.getString("payment_id"));

        return lResult;
    }

    /**
     * Get a sale
     * 
     * @param aConnector
     * @param aToken
     * @return Sale
     * @throws Exception
     */
    public Sale getSale(WebSocketConnector aConnector, Token aToken) throws Exception {
        Sale lResult = Sale.get(getAccessToken(), aToken.getString("transaction_id"));

        return lResult;
    }

    /**
     * Refund a sale
     * 
     * @param aConnector
     * @param aToken
     * @return Refund
     * @throws Exception
     */
    public Refund getRefundSale(WebSocketConnector aConnector, Token aToken) throws Exception {
        String lAccessToken = getAccessToken();

        Sale lSale = Sale.get(lAccessToken, aToken.getString("transaction_id"));
        Map lAmountData = (Map) aToken.getMap("amount");
        Amount lAmount = new Amount();
        lAmount.setCurrency((String) lAmountData.get("currency"));
        lAmount.setTotal((String) lAmountData.get("total"));

        Refund lRefund = new Refund();
        lRefund.setAmount(lAmount);

        return lSale.refund(lAccessToken, lRefund);
    }
}
