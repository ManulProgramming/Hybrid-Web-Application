package com.example.manultube.repository;

import com.example.manultube.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUser_IdAndPost_Id(Long userId, Long postId);

    void deleteByUser_IdAndPost_Id(Long userId, Long postId);

    void deleteByUser_Id(Long userId);

    void deleteByPost_Id(Long postId);

    @Query("""
        SELECT r.post.id,
               SUM(CASE WHEN r.rating = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN r.rating = false THEN 1 ELSE 0 END)
        FROM Rating r
        GROUP BY r.post.id
    """)
    List<Map<String, Object>> getRatingSummary();
}