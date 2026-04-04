package com.example.manultube.service;

import com.example.manultube.dto.Comment.CommentRequestDTO;
import com.example.manultube.dto.Comment.CommentResponseDTO;
import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.Post.PostUpdateDTO;
import com.example.manultube.dto.Rating.RatingRequestDTO;
import com.example.manultube.dto.Rating.RatingResponseDTO;
import com.example.manultube.mapper.CommentMapper;
import com.example.manultube.mapper.PostMapper;
import com.example.manultube.mapper.RatingMapper;
import com.example.manultube.model.*;
import com.example.manultube.mapper.PageMapper;
import com.example.manultube.repository.CommentRepository;
import com.example.manultube.repository.PostRepository;
import com.example.manultube.repository.RatingRepository;
import com.example.manultube.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private static final Path POST_DIR = Paths.get("uploads/p").toAbsolutePath();
    public PostService(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository, RatingRepository ratingRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
    }
    public Page<PostResponseDTO> getAll(Integer page, Integer size, String query, String sort) {
        Pageable pageable = PageRequest.of(page-1, size);
        String sorting;
        if (Objects.equals(sort, "latest")){
            sorting = "latest";
        }else{
            sorting = "hot";
        }
        org.springframework.data.domain.Page<Post> result =
                postRepository.search(query, sorting, pageable);

        return PageMapper.toCustomPage(result, PostMapper::toDto);
    }
    public PostResponseDTO getById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        return PostMapper.toDto(post);
    }
    public Page<PostResponseDTO> getByUserId(Long UserId, Integer page, Integer size, String sort) {
        Pageable pageable = Pageable.unpaged();
        String sorting;
        if (Objects.equals(sort, "latest")){
            sorting = "latest";
        }else{
            sorting = "hot";
        }
        if (size!=null){
            pageable = PageRequest.of(page-1, size);
        }

        org.springframework.data.domain.Page<Post> result =
                postRepository.findByUserId(UserId, sorting, pageable);

        return PageMapper.toCustomPage(result, PostMapper::toDto);
    }
    public PostResponseDTO create(PostRequestDTO postRequestDTO) {
        Post post = new Post();
        post.setUserId(postRequestDTO.getUserId());
        post.setUsername(postRequestDTO.getUsername());
        post.setTitle(postRequestDTO.getTitle());
        post.setDescription((postRequestDTO.getDescription()!=null ? postRequestDTO.getDescription() : ""));
        post.setUpvotes(0);
        post.setDownvotes(0);
        post.setCreatedAt(System.currentTimeMillis());
        return PostMapper.toDto(postRepository.save(post));
    }
    public void update(Long id, PostUpdateDTO postUpdateDTO) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setTitle(postUpdateDTO.getTitle());
            post.setDescription(postUpdateDTO.getDescription());
            postRepository.save(post);
        }
    }
    public void delete(Long id) {
        try {
            Files.deleteIfExists(POST_DIR.resolve(id.toString()));
            Files.deleteIfExists(POST_DIR.resolve(id.toString()+".jpg"));
        }catch (IOException ignored){
        }
        postRepository.deleteById(id);
    }
    @Transactional
    public void deleteAllPosts(Long userId) {
        List<Long> ids = getByUserId(userId, 0, null, "").getContent().stream().map(PostResponseDTO::getId).toList();
        for (Long postId : ids) {
            deleteAllCommentsByPost(postId);
            deleteRatingsForPost(postId);
            try {
                Files.deleteIfExists(POST_DIR.resolve(postId.toString()));
                Files.deleteIfExists(POST_DIR.resolve(postId.toString()+".jpg"));
            }catch (IOException ignored){
            }
        }
        postRepository.deleteByUserId(userId);
    }
    public void updateRatingsForPosts() {
        List<Map<String, Object>> current = ratingRepository.getRatingSummary();
        for (Map<String, Object> map : current) {
            Post post = postRepository.findById((Long) map.get("postId")).orElse(null);
            if (post != null) {
                post.setUpvotes((Integer) map.get("upvotes"));
                post.setDownvotes((Integer) map.get("downvotes"));
                postRepository.save(post);
            }
        }
    }
    public RatingResponseDTO getRating(Long userId, Long postId) {
        Rating rating = ratingRepository.findByUser_IdAndPost_Id(userId, postId).orElse(null);
        if (rating != null) {
            return RatingMapper.toDto(rating);
        }
        return null;
    }
    @Transactional
    public RatingResponseDTO changeRating(RatingRequestDTO rating) {
        User user = userRepository.findById(rating.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(rating.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        RatingResponseDTO currentRating = getRating(user.getId(), post.getId());
        if (currentRating == null) {
            Rating r = new Rating();
            r.setUser(user);
            r.setPost(post);
            r.setRating(rating.getRating());
            currentRating = RatingMapper.toDto(ratingRepository.save(r));
            if (rating.getRating()) {
                post.setUpvotes(post.getUpvotes() + 1);
            } else {
                post.setDownvotes(post.getDownvotes() + 1);
            }
            postRepository.save(post);
        }else{
            if (currentRating.getRating() == rating.getRating()) {
                ratingRepository.deleteByUser_IdAndPost_Id(user.getId(), post.getId());
                if (rating.getRating()) {
                    post.setUpvotes(post.getUpvotes() - 1);
                } else {
                    post.setDownvotes(post.getDownvotes() - 1);
                }
                postRepository.save(post);
                currentRating = null;
            }else{
                Rating r = new Rating();
                r.setId(currentRating.getId());
                r.setUser(user);
                r.setPost(post);
                r.setRating(rating.getRating());
                currentRating = RatingMapper.toDto(ratingRepository.save(r));
                if (rating.getRating()) {
                    post.setUpvotes(post.getUpvotes() + 1);
                    post.setDownvotes(post.getDownvotes() - 1);
                } else {
                    post.setUpvotes(post.getUpvotes() - 1);
                    post.setDownvotes(post.getDownvotes() + 1);
                }
                postRepository.save(post);
            }
        }
        if (currentRating != null) {
            return currentRating;
        }
        return null;
    }
    public void deleteRatingsForPost(Long postId){
        ratingRepository.deleteByPost_Id(postId);
    }
    @Transactional
    public void deleteRatingsForUser(Long userId){
        ratingRepository.deleteByUser_Id(userId);
    }
    public CommentResponseDTO createComment(CommentRequestDTO comment) {
        User user = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Comment com = new Comment();
        com.setUser(user);
        com.setPost(post);
        com.setComment(comment.getComment());
        return CommentMapper.toDto(commentRepository.save(com));
    }
    public Page<CommentResponseDTO> getComments(Long pageId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        org.springframework.data.domain.Page<Comment> result =
                commentRepository.findByPostId(pageId, pageable);
        return PageMapper.toCustomPage(result, CommentMapper::toDto);
    }
    public CommentResponseDTO getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null) {
            return CommentMapper.toDto(comment);
        }
        return null;
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
    @Transactional
    public void deleteAllCommentsByUser(Long userId) {
        commentRepository.deleteByUserId(userId);
    }
    @Transactional
    public void deleteAllCommentsByPost(Long postId) {
        commentRepository.deleteByPostId(postId);
    }
}
