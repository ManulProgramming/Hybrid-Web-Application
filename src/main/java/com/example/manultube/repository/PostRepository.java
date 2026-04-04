package com.example.manultube.repository;

import com.example.manultube.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
    SELECT * FROM posts
    WHERE user_id = :userId
    ORDER BY
       CASE WHEN :sort = 'latest' THEN created_at END DESC,
       CASE WHEN :sort = 'hot' THEN
           LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
           SIGN(upvotes - downvotes) *
           ((created_at / 1000) - 1134028003) / 45000.0
       END DESC,
       id DESC
""",
            countQuery = """
    SELECT COUNT(*) FROM posts
    WHERE user_id = :userId
""",
            nativeQuery = true)
    Page<Post> findByUserId(
            @Param("userId") Long userId,
            @Param("sort") String sort,
            Pageable pageable
    );

    void deleteByUserId(Long userId);

    @Query(value = """
    SELECT * FROM posts
    WHERE LOWER(username) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(title) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY
       CASE WHEN :sort = 'latest' THEN created_at END DESC,
       CASE WHEN :sort = 'hot' THEN
           LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
           SIGN(upvotes - downvotes) *
           ((created_at / 1000) - 1134028003) / 45000.0
       END DESC,
       id DESC
""",
            countQuery = """
    SELECT COUNT(*) FROM posts
    WHERE LOWER(username) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(title) LIKE LOWER(CONCAT('%', :query, '%'))
       OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
""",
            nativeQuery = true)
    Page<Post> search(
            @Param("query") String query,
            @Param("sort") String sort,
            Pageable pageable
    );

}
