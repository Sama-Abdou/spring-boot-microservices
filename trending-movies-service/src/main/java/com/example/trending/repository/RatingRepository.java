package com.example.trending.repository;

import com.example.trending.models.Rating; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    // Aggregate ratings by movieId, order by average descending, limit to top N
    @Query(value = "SELECT movie_id, AVG(rating) as avg_rating " +
                   "FROM ratings " +
                   "GROUP BY movie_id " +
                   "ORDER BY avg_rating DESC " +
                   "LIMIT :topN", nativeQuery = true)
    List<Object[]> findTopRatedMovies(@Param("topN") int topN);
}