package com.ssafy.duck.domain.chat.dto.response;

import com.ssafy.duck.domain.chat.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ChatRes {

    private Long groupChatId;
    private Long manitoChatId;
    private Long manitiChatId;

}
