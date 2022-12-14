package com.project.sns.controller;

import com.project.sns.controller.request.PostCommentRequest;
import com.project.sns.controller.request.PostModifyRequest;
import com.project.sns.controller.request.PostWriteRequest;
import com.project.sns.controller.response.CommentResponse;
import com.project.sns.controller.response.PostResponse;
import com.project.sns.controller.response.Response;
import com.project.sns.model.Comment;
import com.project.sns.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Response<Void> create(@RequestBody PostWriteRequest request, Authentication authentication) {
        postService.create(authentication.getName(), request.getTitle(), request.getBody());
        return Response.success();
    }

    @GetMapping
    public Response<Page<PostResponse>> list(Pageable pageable, Authentication authentication) {
        return Response.success(postService.list(pageable).map(PostResponse::fromPost));
    }

    @GetMapping("/my")
    public Response<Page<PostResponse>> myPosts(Pageable pageable, Authentication authentication) {
        return Response.success(postService.my(authentication.getName(), pageable).map(PostResponse::fromPost));
    }

    @PutMapping("/{postId}")
    public Response<PostResponse> modify(@PathVariable Integer postId, @RequestBody PostModifyRequest request, Authentication authentication) {
        return Response.success(
                PostResponse.fromPost(
                        postService.modify(request.getTitle(), request.getBody(), authentication.getName(), postId)));
    }

    @DeleteMapping("/{postId}")
    public Response<Void> delete(@PathVariable Integer postId, Authentication authentication) {
        postService.delete(authentication.getName(), postId);
        return Response.success();
    }

    @PostMapping("/{postId}/likes")
    public Response<Void> like(@PathVariable Integer postId, Authentication authentication) {

        postService.like(postId, authentication.getName());
        return Response.success();
    }

    @GetMapping("/{postId}/likes")
    public Response<Long> likeCount(@PathVariable Integer postId, Authentication authentication) {

        return Response.success(postService.likeCount(postId));
    }

    @PostMapping("{postId}/comments")
    public Response<Void> comment(@PathVariable Integer postId, @RequestBody PostCommentRequest request, Authentication authentication) {
        postService.comment(postId, authentication.getName(), request.getComment());
        return Response.success();
    }

    @GetMapping("/{postId}/comments")
    public Response<Page<CommentResponse>> comment(@PathVariable Integer postId, Pageable pageable, Authentication authentication) {
        return Response.success(postService.getComments(postId,pageable).map(CommentResponse::fromComment));
    }
}