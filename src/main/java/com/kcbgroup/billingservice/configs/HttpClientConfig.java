package com.kcbgroup.billingservice.configs;

import com.kcbgroup.billingservice.client.OrderClient;
import com.kcbgroup.billingservice.service.impl.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class HttpClientConfig {

    @Value("${safaricom.baseurl}")
    private String safBaseUrl;

    private final TokenManager tokenManager;

    @Bean
    public WebClient orderWebClient() {
        return WebClient.builder()
                .baseUrl(safBaseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .filter(authorizationHeaderFilter()) // Inject token dynamically
                .filter(loggingFilter())             // Log request/response
                .build();
    }

    private ExchangeFilterFunction authorizationHeaderFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request ->
                tokenManager.getValidAccessToken()
                        .map(token -> {
                            return ClientRequest.from(request)
                                    .headers(headers -> headers.setBearerAuth(token))
                                    .build();
                        })
        );
    }

    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}: {}", name, value)));
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response:: Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        }));
    }

    @Bean
    public OrderClient Client(WebClient orderWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(orderWebClient))
                .build();
        return factory.createClient(OrderClient.class);
    }
}