package com.kcbgroup.billingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
public class SafaricomStkPushResponse {
    @JsonProperty("MerchantRequestID")
    private String merchantRequestID;

    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestID;

    @JsonProperty("ResponseCode")
    private String responseCode;

    @JsonProperty("ResponseDescription")
    private String responseDescription;

    @JsonProperty("CustomerMessage")
    private String customerMessage;
}
