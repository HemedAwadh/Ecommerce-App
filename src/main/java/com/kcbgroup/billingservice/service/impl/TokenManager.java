package com.kcbgroup.billingservice.service.impl;

import com.kcbgroup.billingservice.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
public class TokenManager {

    @Value("${auth.token-endpoint}")
    private String tokenEndpoint;

    @Value("${auth.client-id}")
    private String clientId;

    @Value("${auth.client-secret}")
    private String clientSecret;

    private volatile TokenResponse tokenResponse;
    private volatile Instant tokenExpiry;
    private final Object tokenLock = new Object();

    public Mono<String> getValidAccessToken() {
        if (isTokenValid()) {
            log.debug("Using cached access token");
            return Mono.just(tokenResponse.getAccess_token());
        }

        synchronized (tokenLock) {
            if (isTokenValid()) {
                return Mono.just(tokenResponse.getAccess_token());
            }
            return obtainNewToken();
        }
    }

    private boolean isTokenValid() {
        return tokenResponse != null &&
                tokenExpiry != null &&
                Instant.now().isBefore(tokenExpiry.minusSeconds(30));
    }

    private Mono<String> obtainNewToken() {
        String credentials = clientId + ":" + clientSecret;
        String basicHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        log.info("Requesting new token from [{}]", tokenEndpoint);

        WebClient webClient = WebClient.builder().build();

        return webClient.get()
                .uri(tokenEndpoint)
                .header(HttpHeaders.AUTHORIZATION, basicHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnRequest(req -> log.debug("Sending token request"))
                .doOnSuccess(response -> log.info("Received new token: {}", response))
                .doOnError(error -> {
                    log.error("Token request failed: {}", error.getMessage(), error);
                    clearToken(); // clear only on failure
                })
                .map(token -> {
                    updateToken(token);
                    return token.getAccess_token();
                });
    }

    private void updateToken(TokenResponse token) {
        try {
            int expiresIn = Integer.parseInt(token.getExpires_in());
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
        } catch (NumberFormatException e) {
            log.warn("Invalid expires_in value: {}, defaulting to 300s", token.getExpires_in());
            this.tokenExpiry = Instant.now().plusSeconds(300);
        }
        this.tokenResponse = token;
        log.debug("Token updated successfully, expires at {}", tokenExpiry);
    }

    public void clearToken() {
        synchronized (tokenLock) {
            this.tokenResponse = null;
            this.tokenExpiry = null;
        }
        log.info("Access token cache cleared.");
    }
}