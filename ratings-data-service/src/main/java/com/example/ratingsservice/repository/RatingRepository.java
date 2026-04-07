package com.example.ratingsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ratingsservice.models.Rating;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByUserId(String userId);
}