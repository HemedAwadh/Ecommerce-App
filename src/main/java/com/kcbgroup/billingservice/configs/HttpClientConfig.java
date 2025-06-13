package com.kcbgroup.billingservice.configs;

import com.kcbgroup.billingservice.client.OrderClient;
import com.kcbgroup.billingservice.interceptor.TokenAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
@Slf4j
public class HttpClientConfig {

    @Value("${safaricom.baseurl}")
    private String BASE_URL;

    @Bean
    public WebClient orderWebClient(TokenAuthenticationFilter tokenFilter) {
        HttpClient httpClient = HttpClient.create()
                .doOnRequest((req, conn) -> {
                    req.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                    req.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
                    log.info("HTTP Request [{}] --> Method: {}, URI: {}, Headers: ",
                            req.method(), req.uri(), req.requestHeaders());
                })
                .doOnResponse((res, conn) -> {
                    log.info("HTTP Response <-- Status: {}, Headers: {}",
                            res.status(), res.responseHeaders());
                })
                .wiretap(true);

        return WebClient.builder()
                .filter(tokenFilter)
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public OrderClient Client(WebClient orderWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(orderWebClient))
                .build();
        return factory.createClient(OrderClient.class);
    }
}
