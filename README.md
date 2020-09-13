## Justification

Technologies:
 - Spring boot webflux
 - Resilience4j
 - Micrometer, Prometheus, Grafana
 - Lombok
 - Testcontainers
 - React (UI)
 - Docker

Mechanism/architecture:
 - Non-blocking reactive mechanism (Spring boot webflux) was chosen to call downstream services in parallel to address performance of the application;
 - Resilience and stability is provided by resilience4j library with Circuit-breaker, Bulklhead and Retry patterns. For configuration look into application.yml;
 - Micrometer, Prometheus were used to collect performance metrics of both GoogleApi and AppleApi services and Grafana to display the metrics;  
 - Lombok to reduce boiler-plate code;
 - Testcontainers for integration testing with MockServer;
 - Simple React application was developed to display results of back-end service communicated via REST;
 - Docker-compose file was created with 3 applications (Prometheus, Grafana and spring-boot-app itself) to simplify setup process;
 
 ## Swagger
 
 http://localhost:8080/swagger-ui.html
 
 ### EndPoints
 
 Provides 1 http endpoint that accepts text input
 endpoint:
 - POST <host>/v1/booksAlbums
 
 ```sh
 Content-Type: text/plain
 Request-Body: Thank you scientist
 ```
 
 Response is list of items with fields:
  - title of the book or album
  - authors of the book (null in case of album)
  - artist of the album (null in case of book)
  - album (boolean) "true" if item is an album, else "false"
  - book (boolean) "true" if item is a book, else "false"
 
 ```sh
 Accept: application/json
 Content-Type: application/json
{
    "items": [
        {
            "title": "Maps of Non-Existent Places",
            "authors": null,
            "artist": "Thank You Scientist",
            "album": true,
            "book": false
        },
        {
            "title": "A Scientist's Guide to Talking with the Media",
            "authors": [
                 "Richard Hayes",
                 "Daniel Grossman"
            ],
            "artist": null,
            "album": false,
            "book": true
        },
     ...
    ]
}
 ```
 
 ### How to run
 
 Prerequisites:
  - java 11
  - Maven 3.x
  - npm
  - Docker (in case of Docker toolbox configure port forwarding and mount volumes manually)
  - Docker-compose
 
 #### 1. Build spring-boot-app:
  - mvn clean install
 
 #### 2. Build docker image:
  - mvn spring-boot:build-image
  
 #### 3. Start spring-boot-app and monitoring apps:
  - cd ${project-dir}
  - docker-compose up
  - check endpoint http://localhost:8080 
 
 #### 4. Start React UI app (accept 3001 port for the application):
  - cd ${project-dir}/simple-react-ui
  - npm install react-scripts --save
  - npm start
 
 #### 5. Open browser and navigate to http://localhost:3001 
 You should see results for input (hardcoded in ${project-dir}/simple-react-ui/src/components/BooksAlbumsComponent.js)
 
 #### 6. Check apps health in Prometheus:
  - Open http://localhost:9090/ (Status -> Targets) both apps should be running
 
 #### 7. Open Grafana monitoring dashboard at  http://localhost:3000
  - login (admin, admin)
  - Add datasource, select Prometheus, type URL http://localhost:9090 and select access Browser. Save
  - Import dashboard: "+" -> Import -> Upload JSON file -> ${project-dir}/docker/grafana/dashboard.json. Import.
 
  
  
  