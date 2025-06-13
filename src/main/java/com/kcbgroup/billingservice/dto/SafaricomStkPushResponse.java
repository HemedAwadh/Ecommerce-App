package com.kcbgroup.billingservice.dto;

import lombok.Data;

@Data
public class SafaricomStkPushResponse {
    private String MerchantRequestID;
    private String CheckoutRequestID;
    private String ResponseCode;
    private String ResponseDescription;
    private String CustomerMessage;
}
