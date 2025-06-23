package com.kcbgroup.billingservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
public class TokenResponse {
    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("expires_in")
    private String expires_in;
}

