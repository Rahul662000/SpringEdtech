package com.edtech.auth.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;

@Configuration
public class RazorpayConfig {

    @Value("${KEY_ID_RAZORPAY}")
    private String keyId;

    @Value("${KEY_SECRET_RAZORPAY}")
    private String keySecret;
    
    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        System.out.println(keyId);
        System.out.println(keySecret);
        return new RazorpayClient(keyId, keySecret); 
    }

}
