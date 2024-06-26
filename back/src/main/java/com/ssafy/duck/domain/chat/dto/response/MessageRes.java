package com.ssafy.duck.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MessageRes {

    private String id;
    private boolean messageType;
    private String content;
    private String createdTime;
    private Integer senderId;
    private Integer chatId;

}
