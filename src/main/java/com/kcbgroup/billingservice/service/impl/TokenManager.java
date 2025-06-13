package com.kcbgroup.billingservice.service.impl;

import com.kcbgroup.billingservice.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            log.info("AccessToken: {}", tokenResponse.getAccess_token());
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
                Instant.now().isBefore(tokenExpiry.minusSeconds(30)); // 30s buffer
    }

    private Mono<String> obtainNewToken() {
        WebClient webClient = WebClient.builder().build();

        String credential = clientId + ":" + clientSecret;
        String basicHeader = "Basic " + Base64.getEncoder().encodeToString(credential.getBytes());

        return webClient.post()
                .uri(tokenEndpoint)
                .header(HttpHeaders.AUTHORIZATION, basicHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(this::updateToken)
                .doOnSuccess(tokenResponse -> {
                    log.info("New access token: {}", tokenResponse.getAccess_token());
                })
                .map(TokenResponse::getAccess_token)
                .doOnError(error -> {
                    log.error("Error while obtaining new access token: {}", error.getMessage());
                    // Log error and clear current token
                    tokenResponse = null;
                    tokenExpiry = null;
                })
                .doOnRequest((req) -> {
                    log.info("HTTP Request [{}] --> ", req);
                });
    }

    private void updateToken(TokenResponse token) {
        log.info("Updating access token: {}", token.getAccess_token());
        this.tokenResponse = token;
        this.tokenExpiry = Instant.now().plusSeconds(Integer.parseInt(token.getExpires_in()));
    }

    public void clearToken() {
        synchronized (tokenLock) {
            tokenResponse = null;
            tokenExpiry = null;
        }
    }
}

