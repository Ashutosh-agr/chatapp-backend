package com.ashu.chatapp.message;

import com.ashu.chatapp.file.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageMapper {


    MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .type(message.getType())
                .state(message.getState())
                .createdAt(message.getCreatedDate())
                .mediaUrl(message.getMediaUrl())
                .build();
    }
}
