package com.kcbgroup.billingservice.interceptor;

import com.kcbgroup.billingservice.service.impl.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TokenAuthenticationFilter implements ExchangeFilterFunction {

    private final TokenManager tokenManager;

    public TokenAuthenticationFilter(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        return attachTokenAndExchange(request, next)
                .onErrorResume(WebClientResponseException.Unauthorized.class, unauthorized ->
                        retryWithNewToken(request, next)
                );
    }

    private Mono<ClientResponse> attachTokenAndExchange(ClientRequest request, ExchangeFunction next) {
        return tokenManager.getValidAccessToken()
                .map(token -> ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(token))
                        .build())
                .flatMap(next::exchange);
    }

    private Mono<ClientResponse> retryWithNewToken(ClientRequest request, ExchangeFunction next) {
        tokenManager.clearToken(); // Clear and fetch a new token
        return tokenManager.getValidAccessToken()
                .map(newToken -> ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(newToken))
                        .build())
                .flatMap(next::exchange);
    }
}
