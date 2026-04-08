# Spring Boot Microservices

## A. Change Ratings Service Storage Data Model to MySQL

### Step 1 - Set Up MySQL Database

#### 1.1 Create the Database and Table
```sql
CREATE DATABASE ratingsdb;
USE ratingsdb;

CREATE TABLE ratings (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId  VARCHAR(255),
    movieId VARCHAR(255),
    rating  INT NOT NULL
);
```

#### 1.2 Insert Test Data
```sql
INSERT INTO ratings (userId, movieId, rating) VALUES ('user1', '550', 4);
INSERT INTO ratings (userId, movieId, rating) VALUES ('user1', '551', 3);
INSERT INTO ratings (userId, movieId, rating) VALUES ('user1', '552', 5);
INSERT INTO ratings (userId, movieId, rating) VALUES ('user2', '553', 4);
INSERT INTO ratings (userId, movieId, rating) VALUES ('user2', '554', 2);
```
> Movie IDs like 550, 551 are real IDs from The Movie DB (e.g., 550 = Fight Club).

---

### Step 2 - Update pom.xml

Add these two dependencies inside `<dependencies>` in `ratings-data-service/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>
```
> The `<version>` tag is required for mysql-connector-j, otherwise Maven will throw an error.

---

### Step 3 - Configure application.properties

Open `ratings-data-service/src/main/resources/application.properties`:

```properties
spring.application.name=ratings-data-service
server.port=8083

eureka.client.service-url.defaultZone=http://localhost:8761/eureka

spring.datasource.url=jdbc:mysql://localhost:3306/ratingsdb
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

| Property | Description |
|----------|-------------|
| `spring.application.name` | Name used to register this service in Eureka |
| `server.port` | This service listens on port 8083 |
| `eureka.client.service-url` | Address of the Eureka discovery server |
| `spring.datasource.url` | JDBC connection URL - points to your ratingsdb database |
| `spring.datasource.username/password` | Your MySQL login credentials |
| `driver-class-name` | MySQL Connector/J JDBC driver class |
| `ddl-auto=update` | Auto-creates or updates the table on startup |
| `show-sql=true` | Prints SQL queries in the terminal for debugging |
| `hibernate.dialect` | Tells Hibernate to generate MySQL 8 compatible SQL |
| `naming.physical-strategy` | Keeps column names as-is (userId stays userId, not user_id) |

---

### Step 4 - Update Rating.java (JPA Entity)

`ratings-data-service/src/main/java/com/example/ratingsservice/models/Rating.java`

```java
package com.example.ratingsservice.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId")
    private String userId;

    @Column(name = "movieId")
    private String movieId;

    @Column(name = "rating")
    private int rating;

    public Rating() {}
}
```
> Always import `@Id` from `javax.persistence.Id` - NOT from `org.springframework.data.annotation.Id` (that is for MongoDB).

---

### Step 5 - Create RatingRepository.java

Create a new file in the same models folder:

`ratings-data-service/src/main/java/com/example/ratingsservice/models/RatingRepository.java`

```java
package com.example.ratingsservice.models;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByUserId(String userId);
}
```
> `findByUserId` is automatically translated by Spring to `SELECT * FROM ratings WHERE userId = ?` - no SQL needed.

---

### Step 6 - Update RatingsResource.java

```java
package com.example.ratingsservice.resources;

import com.example.ratingsservice.models.Rating;
import com.example.ratingsservice.models.RatingRepository;
import com.example.ratingsservice.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ratings")
public class RatingsResource {

    @Autowired
    private RatingRepository ratingRepository;

    @RequestMapping("/{userId}")
    public UserRating getRatingsOfUser(@PathVariable String userId) {
        List<Rating> ratings = ratingRepository.findByUserId(userId);
        return new UserRating(userId, ratings);
    }
}
```
---

### Step 7 - Run the Services

Start in this order:

```bash
# 1. Start Eureka first
cd discovery-server
mvn spring-boot:run

# 2. Verify Eureka at http://localhost:8761

# 3. Start ratings service
cd ratings-data-service
mvn spring-boot:run

```

---

### Step 8 - Test the Endpoint

```bash
http://localhost:8083/ratings/user1
```

Response:
```json
{
    "userId": "user1",
    "ratings": [
        { "id": 1, "userId": "user1", "movieId": "550", "rating": 4 },
        { "id": 2, "userId": "user1", "movieId": "551", "rating": 3 },
        { "id": 3, "userId": "user1", "movieId": "552", "rating": 5 }
    ]
}
```
![MySQL Schema](images/ratings_schema.png)
---


---

## B. Cache the MovieDB query results in MongoDB

### Step 1 - Set Up MongoDB Cache

#### 1.1 Verify MongoDB Installation
Ensure MongoDB (v8.x recommended) is running locally on the default port 27017.

```bash
mongod --version
sudo systemctl status mongod
```

#### 1.2 Design the Cache Document Structure
MongoDB is a document-oriented NoSQL database. Our movies collection stores JSON documents mapping to our Java objects.

```json
{
  "_id": "550",
  "name": "Fight Club",
  "description": "A ticking-time-bomb insomniac and a slippery soap salesman...",
  "_class": "com.example.movieinfoservice.models.Movie"
}
```

---

### Step 2 - Update pom.xml

Add the Spring Data MongoDB dependency to `movie-info-service/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

---

### Step 3 - Configure application.properties

Open `movie-info-service/src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/movie_db
```

---

### Step 4 - Update Movie.java (MongoDB Document)

`movie-info-service/src/main/java/com/example/movieinfoservice/models/Movie.java`

```java
package com.example.movieinfoservice.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies")
public class Movie {

    @Id
    private String movieId;
    private String name;
    private String description;

    public Movie() {}

    public Movie(String movieId, String name, String description) {
        this.movieId = movieId;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters...
}
```

---

### Step 5 - Create MovieRepository.java

`movie-info-service/src/main/java/com/example/movieinfoservice/models/MovieRepository.java`

```java
package com.example.movieinfoservice.models;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
}
```

---

### Step 6 - Implement Cache-Aside Logic in MovieResource.java

Modified the controller to implement the **Cache-Aside Pattern**.

```java
@Autowired
private MovieRepository movieRepository;

@RequestMapping("/{movieId}")
public Movie getMovieInfo(@PathVariable("movieId") String movieId) throws InterruptedException {
    
    // 1. Check MongoDB Cache first
    Optional<Movie> cachedMovie = movieRepository.findById(movieId);
    
    if (cachedMovie.isPresent()) {
        System.out.println("CACHE HIT: Returning movie " + movieId + " from MongoDB");
        return cachedMovie.get();
    }

    // 2. Cache Miss: Simulate slow external API delay
    System.out.println("CACHE MISS: Fetching movie " + movieId + " (2s delay)...");
    Thread.sleep(2000); 

    final String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;
    MovieSummary movieSummary = restTemplate.getForObject(url, MovieSummary.class);
    
    Movie movie = new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());

    // 3. Save to MongoDB for future requests
    movieRepository.save(movie);

    return movie;
}
```

---


### Step 7 - Testing

![alt text](images/caching.png)

---

### Step 8 - Discussion Questions

* **Why suggest caching in this service?**
  Movie metadata is static. Caching reduces network latency and prevents redundant API calls.
* **Where else might caching matter?**
  Ideal for read-heavy data (configs, user profiles). Avoid for write-heavy data (live inventory) to prevent staleness.

