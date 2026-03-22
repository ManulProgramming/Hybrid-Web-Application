package com.example.manultube.repository;

import com.example.manultube.model.Comment;
import com.example.manultube.model.Page;
import com.example.manultube.model.Post;
import com.example.manultube.model.Rating;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Objects;

@Repository
public class PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public PostRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createTable() {
        jdbcTemplate.getJdbcOperations().execute("""
        CREATE TABLE IF NOT EXISTS posts (
            id SERIAL PRIMARY KEY,
            userId BIGINT NOT NULL,
            username VARCHAR(50) NOT NULL,
            title VARCHAR(100) NOT NULL,
            description VARCHAR(2000),
            upvotes INT NOT NULL,
            downvotes INT NOT NULL,
            createdAt BIGINT NOT NULL,
            FOREIGN KEY (userId) REFERENCES users(id)
        );
        """);
        jdbcTemplate.getJdbcOperations().execute("""
        CREATE TABLE IF NOT EXISTS rating (
            userId BIGINT NOT NULL,
            postId BIGINT NOT NULL,
            rating BOOLEAN NOT NULL,
            PRIMARY KEY (userId, postId),
            FOREIGN KEY (userId) REFERENCES users(id),
            FOREIGN KEY (postId) REFERENCES posts(id)
        );
        """);
        jdbcTemplate.getJdbcOperations().execute("""
        CREATE TABLE IF NOT EXISTS comments (
            id SERIAL PRIMARY KEY,
            userId BIGINT NOT NULL,
            username VARCHAR(50) NOT NULL,
            postId BIGINT NOT NULL,
            comment VARCHAR(150) NOT NULL,
            FOREIGN KEY (userId) REFERENCES users(id),
            FOREIGN KEY (postId) REFERENCES posts(id)
        );
        """);
    }
    public Post insertPost(Post post) {
        String sql = "INSERT INTO posts (userId, username, title, description, upvotes, downvotes, createdAt) VALUES (:userId, :username, :title, :description, :upvotes, :downvotes, :createdAt)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", post.getUserId())
                .addValue("username", post.getUsername())
                .addValue("title", post.getTitle())
                .addValue("description", post.getDescription())
                .addValue("upvotes", post.getUpvotes())
                .addValue("downvotes", post.getDownvotes())
                .addValue("createdAt", post.getCreatedAt());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        if (rowsAffected != 1) {
            throw new DataAccessResourceFailureException("Could not insert post");
        }
        post.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return post;
    }
    public Post getPostById(long id) {
        String sql = "SELECT * FROM posts WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Post.class));
    }
    public Page<Post> getPostsByUserId(long userId, Integer page, Integer size, String sort) {
        String sql = "SELECT COUNT(*) FROM posts WHERE userId = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        if (total==null){
            total=0L;
        }
        if (size!=null && size<1){
            size=16;
        }
        if (size!=null && size>100){
            size=100;
        }
        if (size!=null && (long) page *size>total){
            page= (int) Math.ceil((double) total /size);
        }
        if (page<1){
            page=1;
        }
        if (!Objects.equals(sort, "latest")){
            sort="hot_score";
        }else{
            sort="createdAt";
        }
        if (size!=null) {
            sql = """
            
                    SELECT *,
                   LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
                   SIGN(upvotes - downvotes) *
                   (
                       (createdAt / 1000) - 1134028003
                       ) / 45000.0
                       AS hot_score
            FROM posts WHERE userId = :userId ORDER BY\s""" +sort+ " DESC, id DESC LIMIT :size OFFSET :page";
            params
                    .addValue("size", size )
                    .addValue("page", (page-1)*size);
        }else{
            sql = """
            
                    SELECT *,
                   LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
                   SIGN(upvotes - downvotes) *
                   (
                       (createdAt / 1000) - 1134028003
                       ) / 45000.0
                       AS hot_score
            FROM posts WHERE userId = :userId
            """;
        }
        List<Post> posts = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Post.class));
        if (size==null){
            size = Integer.MAX_VALUE;
        }
        Page<Post> p = new Page<>();
        p.setContent(posts);
        p.setPage(page);
        p.setSize(size);
        p.setTotalElements(total);
        p.setTotalPages();
        return p;
    }
    public Page<Post> getAllPosts(Integer page, Integer size, String query, String sort) {
        query="%"+query+"%";
        String sql = "SELECT COUNT(*) FROM posts WHERE username ILIKE :query OR title ILIKE :query OR description ILIKE :query";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", query);
        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        if (total==null){
            total=0L;
        }
        if (size!=null && size<1){
            size=16;
        }
        if (size!=null && size>100){
            size=100;
        }
        if (size!=null && (long) page *size>total){
            page= (int) Math.ceil((double) total /size);
        }
        if (page<1){
            page=1;
        }
        if (!Objects.equals(sort, "latest")){
            sort="hot_score";
        }else{
            sort="createdAt";
        }
        if (size!=null) {
            sql = """
            
                    SELECT *,
                   LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
                   SIGN(upvotes - downvotes) *
                   (
                       (createdAt / 1000) - 1134028003
                       ) / 45000.0
                       AS hot_score
            FROM posts WHERE username ILIKE :query OR title ILIKE :query OR description ILIKE :query ORDER BY\s""" + sort + " DESC, id DESC LIMIT :size OFFSET :page";
            params
                    .addValue( "size", size )
                    .addValue( "page", (page-1)*size);
        }else{
            sql = """
            
                    SELECT *,
                   LOG(GREATEST(ABS(upvotes - downvotes), 1)) +
                   SIGN(upvotes - downvotes) *
                   (
                       (createdAt / 1000) - 1134028003
                       ) / 45000.0
                       AS hot_score
            FROM posts WHERE username ILIKE :query OR title ILIKE :query OR description ILIKE :query
            """;
        }
        List<Post> posts = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Post.class));
        if (size==null){
            size = Integer.MAX_VALUE;
        }
        Page<Post> p = new Page<>();
        p.setContent(posts);
        p.setPage(page);
        p.setSize(size);
        p.setTotalElements(total);
        p.setTotalPages();
        return p;
    }
    public void updatePost(Post post) {
        String set_query = "";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", post.getId());
        if (post.getTitle() != null) {
            set_query+=" title = :title ";
            params.addValue("title", post.getTitle());
        }
        if (post.getDescription() != null) {
            if (!set_query.isEmpty()) {
                set_query+=",";
            }
            set_query+=" description = :description ";
            params.addValue("description", post.getDescription());
        }
        if (!set_query.isEmpty()) {
            String sql = "UPDATE posts SET"+set_query+"WHERE id = :id";
            jdbcTemplate.update(sql, params);
        }
    }
    public void deletePost(long id) {
        String sql = "DELETE FROM posts WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, params);
    }
    public void deletePostsForUserId(long userId) {
        String sql = "DELETE FROM posts WHERE userId = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }
    public Rating createRating(Rating rating) {
        String sql = "INSERT INTO rating (userId, postId, rating) VALUES (:userId, :postId, :rating)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", rating.getUserId())
                .addValue("postId", rating.getPostId())
                .addValue("rating", rating.getRating());
        int rowsAffected = jdbcTemplate.update(sql, params);
        if (rowsAffected != 1) {
            throw new DataAccessResourceFailureException("Could not insert post");
        }
        return rating;
    }
    public Rating updateRating(Rating rating) {
        String sql = "UPDATE rating SET rating = :rating WHERE userId = :userId AND postId = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("rating", rating.getRating())
                .addValue("userId", rating.getUserId())
                .addValue("postId", rating.getPostId());
        int rowsAffected = jdbcTemplate.update(sql, params);
        if (rowsAffected != 1) {
            throw new DataAccessResourceFailureException("Could not update post");
        }
        return rating;
    }
    public Rating getRating(Long userId, Long postId) {
        String sql = "SELECT * FROM rating WHERE userId = :userId AND postId = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("postId", postId);
        try {
            return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Rating.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public void deleteRating(Long userId, Long postId) {
        String sql = "DELETE FROM rating WHERE userId = :userId and postId = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("postId", postId);
        jdbcTemplate.update(sql, params);
    }
    public void changeUpvote(Long postId, Integer upvote) {
        String sql = "UPDATE posts SET upvotes = upvotes + :upvote WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", postId)
                .addValue("upvote", upvote);
        jdbcTemplate.update(sql, params);
    }
    public void changeDownvote(Long postId, Integer downvote) {
        String sql = "UPDATE posts SET downvotes = downvotes + :downvote WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", postId)
                .addValue("downvote", downvote);
        jdbcTemplate.update(sql, params);
    }
    public Comment insertComment(Comment comment) {
        String sql = "INSERT INTO comments (userId, username, postId, comment) VALUES (:userId, :username, :postId, :comment)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", comment.getUserId())
                .addValue("username", comment.getUsername())
                .addValue("postId", comment.getPostId())
                .addValue("comment", comment.getComment());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        if (rowsAffected != 1) {
            throw new DataAccessResourceFailureException("Could not insert comment");
        }
        comment.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return comment;
    }
    public Page<Comment> getComments(Long postId, Integer page, Integer size) {
        String sql = "SELECT COUNT(*) FROM comments WHERE postId = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId);
        Long total = jdbcTemplate.queryForObject(sql, params, Long.class);
        if (total==null){
            total=0L;
        }
        if (size<1){
            size=16;
        }
        if (size>100){
            size=100;
        }
        if ((long) page *size>total){
            page= (int) Math.ceil((double) total /size);
        }
        if (page<1){
            page=1;
        }
        sql = "SELECT * FROM comments WHERE postId = :postId LIMIT :size OFFSET :page";
        params
                .addValue("size", size)
                .addValue("page", (page-1)*size);
        List<Comment> comments = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Comment.class));
        Page<Comment> p = new Page<>();
        p.setContent(comments);
        p.setPage(page);
        p.setSize(size);
        p.setTotalElements(total);
        p.setTotalPages();
        return p;
    }
    public Comment getCommentById(Long commentId) {
        String sql = "SELECT * FROM comments WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", commentId);
        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Comment.class));
    }
    public void deleteComment(Long commentId) {
        String sql = "DELETE FROM comments WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", commentId);
        jdbcTemplate.update(sql, params);
    }
}
