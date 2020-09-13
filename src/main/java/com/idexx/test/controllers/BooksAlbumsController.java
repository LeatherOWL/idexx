package com.idexx.test.controllers;

import com.idexx.test.model.BooksAlbumsDTO;
import com.idexx.test.model.google.GoogleApiResponse;
import com.idexx.test.services.AppleApiService;
import com.idexx.test.services.CombineResultsService;
import com.idexx.test.services.GoogleApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/booksAlbums")
@Slf4j
public class BooksAlbumsController {

    private final GoogleApiService googleApiService;
    private final AppleApiService appleApiService;
    private final CombineResultsService combineResultsService;

    public BooksAlbumsController(GoogleApiService googleApiService,
                                 AppleApiService appleApiService, CombineResultsService combineResultsService) {
        this.googleApiService = googleApiService;
        this.appleApiService = appleApiService;
        this.combineResultsService = combineResultsService;
    }

    @PostMapping
    Mono<BooksAlbumsDTO> postForBooksAndAlbums(@RequestBody String term) {
        log.debug("Received request with term: {}", term);
        Mono<GoogleApiResponse> googleApiResponse = googleApiService.getApiResponse(term);
        Mono<String> appleApiResponse = appleApiService.getApiResponse(term);
        return Mono.zip(googleApiResponse, appleApiResponse)
                .flatMap(combineResultsService::combineGoogleAndAppleResponses);
    }
}
