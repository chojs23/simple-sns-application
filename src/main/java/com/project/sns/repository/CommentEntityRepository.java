package com.project.sns.repository;

import com.project.sns.model.entity.CommentEntity;
import com.project.sns.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentEntityRepository extends JpaRepository<CommentEntity, Integer> {

    Page<CommentEntity> findAllByPost(PostEntity post, Pageable pageable);


    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity entity SET removed_at = NOW() where entity.post= :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity); //지울 때 직접 join 쿼리짜면 한번에 수정

    //void deletedAllByPost(PostEntity post); -> comment 쿼리로 comment 불러오고 하나하나씩 update함
}
