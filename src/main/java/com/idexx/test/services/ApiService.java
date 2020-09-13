package com.idexx.test.services;

import reactor.core.publisher.Mono;

public interface ApiService<T> {

    Mono<T> getApiResponse(String term);
}
