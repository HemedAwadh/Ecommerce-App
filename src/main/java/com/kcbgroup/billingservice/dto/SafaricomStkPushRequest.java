package com.kcbgroup.billingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class SafaricomStkPushRequest {
    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("Amount")
    private int amount;

    @JsonProperty("PartyA")
    private String partyA;

    @JsonProperty("PartyB")
    private String partyB;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("CallBackURL")
    private String callbackURL;

    @JsonProperty("AccountReference")
    private String accountReference;

    @JsonProperty("TransactionDesc")
    private String transactionDesc;
}
