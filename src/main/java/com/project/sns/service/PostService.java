package com.project.sns.service;

import com.project.sns.exception.ErrorCode;
import com.project.sns.exception.SnsApplicationException;
import com.project.sns.model.AlarmArgs;
import com.project.sns.model.AlarmType;
import com.project.sns.model.Comment;
import com.project.sns.model.Post;
import com.project.sns.model.entity.*;
import com.project.sns.model.event.AlarmEvent;
import com.project.sns.producer.AlarmProducer;
import com.project.sns.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final AlarmService alarmService;
    private final AlarmProducer alarmProducer;

    @Transactional
    public void create(String userName, String title, String body) {
        UserEntity userEntity = getUserEntityOrException(userName);

        PostEntity postEntity = PostEntity.of(title, body, userEntity);
        postEntityRepository.save(postEntity);
    }

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = getUserEntityOrException(userName);

        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }

    @Transactional
    public Post modify(String title, String body,String userName,Integer postId) {
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(userName);

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with post %d", userName, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);

        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }

    @Transactional
    public void delete(String userName, Integer postId) {
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(userName);

        //post permission
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with post %d", userName, postId));
        }

        likeEntityRepository.deleteAllByPost(postEntity);
        commentEntityRepository.deleteAllByPost(postEntity);
        postEntityRepository.delete(postEntity);
    }


    @Transactional
    public void like(Integer postId, String userName) {
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(userName);

        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it ->{
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED,
            String.format("userName %s already like post %d", userName, postId));
        });

        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));

        // 사용자가 like 하면 alarm 같은 경우는 사용자의 주된 관심사가 아니기 때문에 알람은 비동기적으로 처리해서 성능 향상 kafka 사용
        // 알람을 저장하고 보내는 이벤트를 kafka 로 보냄
//        AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_LIKE_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
//        alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());
        alarmProducer.send(new AlarmEvent(postEntity.getUser().getId(), AlarmType.NEW_LIKE_ON_POST,
                new AlarmArgs(userEntity.getId(), postEntity.getId())));

    }

    public long likeCount(Integer postId) {
        PostEntity postEntity = getPostEntityOrException(postId);

        return likeEntityRepository.countByPost(postEntity);

    }

    @Transactional
    public void comment(Integer postId, String userName,String comment) {
        PostEntity postEntity = getPostEntityOrException(postId);
        UserEntity userEntity = getUserEntityOrException(userName);

        //comment save
        commentEntityRepository.save(CommentEntity.of(userEntity, postEntity, comment));

        //AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
        //alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());
        alarmProducer.send(new AlarmEvent(postEntity.getUser().getId(), AlarmType.NEW_COMMENT_ON_POST,
                new AlarmArgs(userEntity.getId(), postEntity.getId())));
    }


    public Page<Comment> getComments(Integer postId, Pageable pageable) {
        PostEntity postEntity = getPostEntityOrException(postId);
        return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);

    }

    private PostEntity getPostEntityOrException(Integer postId) {
        return postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("postId is %d", postId)));
    }
    private UserEntity getUserEntityOrException(String userName) {
        return userEntityRepository.findByUserName(userName)
                .orElseThrow(() -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("userName is %s", userName)));

    }


}