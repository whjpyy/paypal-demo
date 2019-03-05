package com.example.paypal.service;

import com.example.paypal.config.PaypalPaymentIntent;
import com.example.paypal.config.PaypalPaymentMethod;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalService {

    @Autowired
    private APIContext apiContext;

    /**
     * 创建交易记录
     * @param total
     * @param currency
     * @param method
     * @param intent
     * @param description
     * @param cancelUrl
     * @param successUrl
     * @return
     * @throws PayPalRESTException
     */
    public Payment createPayment(
            Double total, String currency, PaypalPaymentMethod method,
            PaypalPaymentIntent intent, String description, String cancelUrl,
            String successUrl) throws PayPalRESTException {
        // 金额
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

        // 单个交易
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        // 交易列表
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // 付款人
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toString());

        // 商家
        Payment payment = new Payment();
        payment.setIntent(intent.toString());
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // 重定向的url
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl); // 失败页面
        redirectUrls.setReturnUrl(successUrl); // 成功页面
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    /**
     * 执行交易
     * @param paymentId
     * @param payerId
     * @return
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }

}
