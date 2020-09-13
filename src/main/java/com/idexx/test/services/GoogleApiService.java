package com.idexx.test.services;

import com.idexx.test.model.google.GoogleApiResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Service
@Slf4j
public class GoogleApiService implements ApiService {

    private static final String GOOGLE_API = "google-api";

    private final WebClient.Builder webClientBuilder;

    @Value("${api.response.timeout:10}")
    private int apiResponseTimeout;

    @Value("${max.results:5}")
    private int maxResult;

    @Value("${google.api.scheme:https}")
    private String googleApiScheme;

    @Value("${google.api.host:www.googleapis.com}")
    private String googleApiHost;

    @Value("${google.api.port:443}")
    private String googleApiPort;

    public GoogleApiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    @Bulkhead(name = GOOGLE_API)
    @CircuitBreaker(name = GOOGLE_API)
    @Retry(name = GOOGLE_API, fallbackMethod = "googleApiFallback")
    public Mono<GoogleApiResponse> getApiResponse(String term) {
        log.debug("Getting API response from Google with term: {}", term);
        return webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(googleApiScheme)
                        .host(googleApiHost)
                        .port(googleApiPort)
                        .path("/books/v1/volumes")
                        .queryParam("q", term)
                        .queryParam("maxResults", maxResult)
                        .build())
                .retrieve().bodyToMono(GoogleApiResponse.class)
                .timeout(ofSeconds(apiResponseTimeout));
    }

    private Mono<GoogleApiResponse> googleApiFallback(Exception e) {
        return Mono.just(new GoogleApiResponse());
    }
}
