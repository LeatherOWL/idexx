package com.idexx.test.services;

import com.idexx.test.model.BooksAlbumsDTO;
import com.idexx.test.model.Item;
import com.idexx.test.model.apple.AppleApiResponse;
import com.idexx.test.model.google.GoogleApiResponse;
import com.idexx.test.utils.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

@Service
@Slf4j
public class CombineResultsService {

    private final JsonParser jsonParser;

    public CombineResultsService(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public Mono<BooksAlbumsDTO> combineGoogleAndAppleResponses(Tuple2<GoogleApiResponse, String> tuple) {
        log.debug("Start combining results from both services");

        BooksAlbumsDTO result = new BooksAlbumsDTO();

        GoogleApiResponse books = tuple.getT1();
        String albumsString = tuple.getT2();

        Stream<Item> booksStream = convertBooksResponseToItemStream(books);
        Stream<Item> albumsStream = convertAlbumsStringToItemsStream(tuple, albumsString);

        if (booksStream != null && albumsStream != null) {
            result.setItems(Stream.concat(booksStream, albumsStream)
                    .sorted(comparing(Item::getTitle)).toArray(Item[]::new));
        } else if (booksStream != null) {
            result.setItems(booksStream.sorted(comparing(Item::getTitle)).toArray(Item[]::new));
        } else if (albumsStream != null) {
            result.setItems(albumsStream.sorted(comparing(Item::getTitle)).toArray(Item[]::new));
        }

        return Mono.just(result);
    }

    private Stream<Item> convertBooksResponseToItemStream(GoogleApiResponse books) {
        if (books.getItems() != null) {
            log.debug("Books size: {}", books.getItems().length);
            return stream(books.getItems()).map(book -> {
                Item item = new Item();
                item.setBook(true);
                item.setAuthors(book.getVolumeInfo().getAuthors());
                item.setTitle(book.getVolumeInfo().getTitle());
                return item;
            });
        }
        log.debug("Books size: {}", 0);
        return null;
    }

    private Stream<Item> convertAlbumsStringToItemsStream(Tuple2<GoogleApiResponse, String> tuple,
                                                          String albumsString) {
        if (!albumsString.isEmpty()) {
            AppleApiResponse albums = jsonParser.parseJsonString(tuple.getT2(), AppleApiResponse.class);
            if (albums.getResults() != null) {
                log.debug("Albums size: {}", albums.getResults().length);
                return stream(albums.getResults()).map(album -> {
                    Item item = new Item();
                    item.setAlbum(true);
                    item.setArtist(album.getArtistName());
                    item.setTitle(album.getCollectionName());
                    return item;
                });
            }
        }
        log.debug("Albums size: {}", 0);
        return null;
    }
}
