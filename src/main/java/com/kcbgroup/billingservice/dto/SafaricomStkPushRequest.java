package com.kcbgroup.billingservice.dto;

import lombok.Data;

@Data
public class SafaricomStkPushRequest {
    private String businessShortCode;
    private String password;
    private String timestamp;
    private String transactionType;
    private String amount;
    private String partyA;
    private String partyB;
    private String phoneNumber;
    private String callBackURL;
    private String accountReference;
    private String transactionDesc;
}
