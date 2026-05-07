package com.workmate.workmate.communication.dto;

import com.workmate.workmate.communication.entity.Post;
import com.workmate.workmate.user.entity.User;
import com.workmate.workmate.user.entity.Workplace;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private String title;
    private String content;
    private String authorName;   // 작성자 이름
    private String workplaceName; // 업장 이름
    private LocalDateTime createdAt;


    public static PostResponse from(Post post) {

        User user = post.getUser();
        Workplace workplace = post.getBoard().getWorkplace();

        return PostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                // [핵심 로직] 유저의 deleted 상태가 true면 이름을 치환
                .authorName(user.getDeleted() ? "<삭제된 유저>" : user.getName())
                // [핵심 로직] 업장의 deleted 상태가 true면 이름을 치환
                .workplaceName(workplace.getDeleted() ? "<삭제된 업장>" : workplace.getName())
                .createdAt(post.getCreatedAt())
                .build();

    }
}
