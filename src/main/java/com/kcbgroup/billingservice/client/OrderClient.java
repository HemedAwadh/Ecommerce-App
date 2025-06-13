package com.kcbgroup.billingservice.client;

import com.kcbgroup.billingservice.dto.SafaricomStkPushRequest;
import com.kcbgroup.billingservice.dto.SafaricomStkPushResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface OrderClient {
    @PostExchange("/mpesa/stkpush/v1/processrequest")
    SafaricomStkPushResponse processBilling(@RequestBody SafaricomStkPushRequest safaricomStkPushRequest);

}
