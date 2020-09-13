package com.idexx.test.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofSeconds;

@Service
@Slf4j
public class AppleApiService implements ApiService {

    private static final String APPLE_API = "apple-api";

    private final WebClient.Builder webClientBuilder;

    @Value("${api.response.timeout:10}")
    private int apiResponseTimeout;

    @Value("${max.results:5}")
    private int maxResult;

    @Value("${apple.api.scheme:https}")
    private String appleApiScheme;

    @Value("${apple.api.host:itunes.apple.com}")
    private String appleApiHost;

    @Value("${apple.api.port:443}")
    private String appleApiPort;

    public AppleApiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    @Bulkhead(name = APPLE_API)
    @CircuitBreaker(name = APPLE_API)
    @Retry(name = APPLE_API, fallbackMethod = "appleApiFallback")
    public Mono<String> getApiResponse(String term) {
        log.debug("Getting API response from iTunes with term: {}", term);
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(appleApiScheme)
                        .host(appleApiHost)
                        .port(appleApiPort)
                        .path("/search")
                        .queryParam("term", term)
                        .queryParam("entity", "album")
                        .queryParam("limit", maxResult)
                        .build())
                .retrieve().bodyToMono(String.class)
                .timeout(ofSeconds(apiResponseTimeout));
    }

    private Mono<String> appleApiFallback(Exception e) {
        return Mono.just("");
    }
}
