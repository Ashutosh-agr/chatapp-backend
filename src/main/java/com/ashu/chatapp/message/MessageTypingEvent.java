package com.ashu.chatapp.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageTypingEvent {
    private String event;
    private String senderId;
    private String receiverId;
    private String chatId;
}
