package com.project.sns.model;


import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    NEW_COMMENT_ON_POST("new comment"),
    NEW_LIKE_ON_POST("new like!"),
    ;

    private final String alarmText;
}
