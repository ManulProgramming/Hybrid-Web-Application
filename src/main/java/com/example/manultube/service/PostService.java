package com.example.manultube.service;

import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.Post.PostUpdateDTO;
import com.example.manultube.model.Comment;
import com.example.manultube.model.Page;
import com.example.manultube.model.Post;
import com.example.manultube.model.Rating;
import com.example.manultube.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final PostRepository postRepository;
    private static final Path POST_DIR = Paths.get("uploads/p").toAbsolutePath();
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
        postRepository.createTable();
    }
    private PostResponseDTO toDto(Post post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setUsername(post.getUsername());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setUpvotes(post.getUpvotes());
        dto.setDownvotes(post.getDownvotes());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
    public Page<PostResponseDTO> getAll(Integer page, Integer size, String query, String sort) {
        Page<Post> postPage = postRepository.getAllPosts(page, size, query, sort);

        List<PostResponseDTO> dtoList = postPage.getContent().stream().map(this::toDto).toList();
        Page<PostResponseDTO> p = new Page<>();
        p.setContent(dtoList);
        p.setPage(postPage.getPage());
        p.setSize(postPage.getSize());
        p.setTotalElements(postPage.getTotalElements());
        p.setTotalPages();
        return p;
    }
    public PostResponseDTO getById(Long id) {
        return toDto(postRepository.getPostById(id));
    }
    public Page<PostResponseDTO> getByUserId(Long UserId, Integer page, Integer size, String sort) {
        Page<Post> postPage = postRepository.getPostsByUserId(UserId, page, size, sort);

        List<PostResponseDTO> dtoList = postPage.getContent().stream().map(this::toDto).toList();
        Page<PostResponseDTO> p = new Page<>();
        p.setContent(dtoList);
        p.setPage(postPage.getPage());
        p.setSize(postPage.getSize());
        p.setTotalElements(postPage.getTotalElements());
        p.setTotalPages();
        return p;
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
        return toDto(postRepository.insertPost(post));
    }
    public void update(Long id, PostUpdateDTO postUpdateDTO) {
        Post post = new Post();
        post.setTitle(postUpdateDTO.getTitle());
        post.setDescription(postUpdateDTO.getDescription());
        post.setId(id);
        postRepository.updatePost(post);
    }
    public void delete(Long id) {
        try {
            Files.deleteIfExists(POST_DIR.resolve(id.toString()));
            Files.deleteIfExists(POST_DIR.resolve(id.toString()+".jpg"));
        }catch (IOException ignored){
        }
        postRepository.deletePost(id);
    }
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
        postRepository.deletePostsForUserId(userId);
    }
    public void updateRatingsForPosts() {
        List<Map<String, Object>> current = postRepository.getCurrentRatings();
        for (Map<String, Object> map : current) {
            postRepository.updateUpvotesAndDownvotesForPost((Long) map.get("postId"), (Long) map.get("upvotes"), (Long) map.get("downvotes"));
        }
    }
    public Rating getRating(Long userId, Long postId) {
        return postRepository.getRating(userId,postId);
    }
    public Rating changeRating(Rating rating) {
        Rating currentRating = getRating(rating.getUserId(),rating.getPostId());
        if (currentRating == null) {
            currentRating = postRepository.createRating(rating);
            if (rating.getRating()){
                postRepository.changeUpvote(rating.getPostId(),1);
            }else{
                postRepository.changeDownvote(rating.getPostId(),1);
            }
        }else{
            if (currentRating.getRating() == rating.getRating()) {
                postRepository.deleteRating(rating.getUserId(),rating.getPostId());
                if (rating.getRating()){
                    postRepository.changeUpvote(rating.getPostId(),-1);
                }else{
                    postRepository.changeDownvote(rating.getPostId(),-1);
                }
                currentRating = null;
            }else{
                currentRating = postRepository.updateRating(rating);
                if (rating.getRating()){
                    postRepository.changeUpvote(rating.getPostId(),1);
                    postRepository.changeDownvote(rating.getPostId(),-1);
                }else{
                    postRepository.changeDownvote(rating.getPostId(),1);
                    postRepository.changeUpvote(rating.getPostId(),-1);
                }
            }
        }
        return currentRating;
    }
    public void deleteRatingsForPost(Long postId){
        postRepository.deleteRatingForPost(postId);
    }
    public void deleteRatingsForUser(Long userId){
        postRepository.deleteRatingForUser(userId);
    }
    public Comment createComment(Comment comment) {
        return postRepository.insertComment(comment);
    }
    public Page<Comment> getComments(Long pageId, Integer page, Integer size) {
        return postRepository.getComments(pageId, page, size);
    }
    public Comment getCommentById(Long commentId) {
        return postRepository.getCommentById(commentId);
    }

    public void deleteComment(Long commentId) {
        postRepository.deleteComment(commentId);
    }
    public void deleteAllCommentsByUser(Long userId) {
        postRepository.deleteCommentsForUserId(userId);
    }
    public void deleteAllCommentsByPost(Long postId) {
        postRepository.deleteCommentsForPostId(postId);
    }
}
