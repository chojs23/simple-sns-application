package com.project.sns.controller.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCommentRequest {
    private String comment;
}
