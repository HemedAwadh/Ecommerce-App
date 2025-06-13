package com.kcbgroup.billingservice.service;

import com.kcbgroup.billingservice.dto.SafaricomStkCallback;
import org.springframework.http.ResponseEntity;

public interface SafaricomCallBackService {

    ResponseEntity<?>processCallback(SafaricomStkCallback safaricomStkCallback);
}
