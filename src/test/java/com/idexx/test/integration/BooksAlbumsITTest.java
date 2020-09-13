package com.idexx.test.integration;

import com.idexx.test.model.BooksAlbumsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.containers.MockServerContainer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"google.api.scheme=http", "google.api.host=localhost", "google.api.port=1080",
                "apple.api.scheme=http", "apple.api.host=localhost", "apple.api.port=1080",
                "logging.level.org.springframework.web.client.RestTemplate=DEBUG"})
class BooksAlbumsITTest {

    private static MockServerClient mockServerClient;

    @Autowired
    private TestRestTemplate restTemplate;

    static {
        FixedMockServerContainer mockServerContainer = new FixedMockServerContainer();
        mockServerContainer.configurePort();
        mockServerContainer.start();
        mockServerClient = new MockServerClient(mockServerContainer.getContainerIpAddress(),
                mockServerContainer.getServerPort());
    }

    @Value("classpath:responses/valid-google-response-5-climbing.txt")
    Resource validGoogleResponse5Climbing;

    @Value("classpath:responses/valid-apple-response-5-climbing.txt")
    Resource validAppleResponse5Climbing;

    private HttpRequest googleHttpRequest5Climbing = HttpRequest.request()
            .withPath("/books/v1/volumes")
            .withQueryStringParameter("q", "climbing")
            .withQueryStringParameter("maxResults", "5");

    private HttpRequest appleHttpRequest5Climbing = HttpRequest.request()
            .withPath("/search")
            .withQueryStringParameter("term", "climbing")
            .withQueryStringParameter("entity", "album")
            .withQueryStringParameter("limit", "5");

    @BeforeEach
    void init() {
        mockServerClient.clear(googleHttpRequest5Climbing);
        mockServerClient.clear(appleHttpRequest5Climbing);
    }

    @Test
    @DisplayName("Both services return good response")
    void testGood() {
        mockServerClient.when(googleHttpRequest5Climbing).respond(response()
                .withHeader("Content-Type", "application/json")
                .withBody(asString(validGoogleResponse5Climbing)));
        mockServerClient.when(appleHttpRequest5Climbing)
                .respond(response().withBody(asString(validAppleResponse5Climbing)));

        ResponseEntity<BooksAlbumsDTO> response =
                restTemplate.postForEntity("/v1/booksAlbums", "climbing", BooksAlbumsDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).hasSize(10);
    }

    @Test
    @DisplayName("No response from Google Api")
    void testGoogleNoResponse() {

        mockServerClient.when(googleHttpRequest5Climbing).respond(response().withStatusCode(500));
        mockServerClient.when(appleHttpRequest5Climbing)
                .respond(response().withBody(asString(validAppleResponse5Climbing)));

        ResponseEntity<BooksAlbumsDTO> response =
                restTemplate.postForEntity("/v1/booksAlbums", "climbing", BooksAlbumsDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).hasSize(5);
    }

    @Test
    @DisplayName("No response from Apple Api")
    void testAppleNoResponse() {
        mockServerClient.when(googleHttpRequest5Climbing).respond(response()
                .withHeader("Content-Type", "application/json")
                .withBody(asString(validGoogleResponse5Climbing)));
        mockServerClient.when(appleHttpRequest5Climbing).respond(response().withStatusCode(500));

        ResponseEntity<BooksAlbumsDTO> response =
                restTemplate.postForEntity("/v1/booksAlbums", "climbing", BooksAlbumsDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).hasSize(5);
    }

    @Test
    @DisplayName("No response from Google Api and Apple Api")
    void testBothServicesNoResponse() {

        mockServerClient.when(googleHttpRequest5Climbing).respond(response().withStatusCode(500));
        mockServerClient.when(appleHttpRequest5Climbing).respond(response().withStatusCode(500));

        ResponseEntity<BooksAlbumsDTO> response =
                restTemplate.postForEntity("/v1/booksAlbums", "climbing", BooksAlbumsDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).isNull();
    }

    public static class FixedMockServerContainer extends MockServerContainer {
        FixedMockServerContainer() {
            super();
        }

        void configurePort() {
            super.addFixedExposedPort(1080, 1080);
        }
    }

    String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
