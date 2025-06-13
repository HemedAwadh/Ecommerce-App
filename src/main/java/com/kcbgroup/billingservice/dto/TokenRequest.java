package com.kcbgroup.billingservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TokenRequest {
    private String client_id;
    private String client_secret;
    private String grant_type;

}

