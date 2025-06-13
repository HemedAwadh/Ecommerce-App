// SafaricomCallbackController.java
package com.kcbgroup.billingservice.controller;

import com.kcbgroup.billingservice.dto.SafaricomStkCallback;
import com.kcbgroup.billingservice.service.SafaricomCallBackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/callback")
@Slf4j
@RequiredArgsConstructor
public class SafaricomCallbackController {

    private final SafaricomCallBackService safaricomCallBackService;

    @PostMapping("/stkpush")
    public ResponseEntity<?> handleStkCallback(@RequestBody SafaricomStkCallback callback) {
        log.info("Received STK Callback: {}", callback);

        var response = safaricomCallBackService.processCallback(callback);
        return ResponseEntity.ok(response);
    }
}
