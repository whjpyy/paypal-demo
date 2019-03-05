package com.example.paypal.controller;

import com.example.paypal.config.PaypalPaymentIntent;
import com.example.paypal.config.PaypalPaymentMethod;
import com.example.paypal.service.PaypalService;
import com.example.paypal.util.URLUtils;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class PaymentController {

    public static final String PAYPAL_SUCCESS_URL = "pay/success";
    public static final String PAYPAL_CANCEL_URL = "pay/cancel";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PaypalService paypalService;

    @GetMapping("")
    public String index(){
        return "index";
    }

    @GetMapping(PAYPAL_CANCEL_URL)
    public String cancelPay(){
        return "cancel";
    }

    @GetMapping(PAYPAL_SUCCESS_URL)
    public String success(@RequestParam("paymentId") String paymentId,
                          @RequestParam("PayerID") String payerId){
        try{
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){
                return "success";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @PostMapping("pay")
    public String pay(HttpServletRequest request){
        String successUrl = URLUtils.getBaseURI(request) + PAYPAL_SUCCESS_URL;
        String cancelUrl = URLUtils.getBaseURI(request) + PAYPAL_CANCEL_URL;

        try{
            Payment payment = paypalService.createPayment(1.2, "USD",
                    PaypalPaymentMethod.paypal, PaypalPaymentIntent.sale,
                    "payment description", cancelUrl, successUrl);

            for(Links link: payment.getLinks()){
                if(link.getRel().equals("approval_url")){
                    return "redirect:" + link.getHref();
                }
            }

        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }

        return "redirect:/";
    }
}
