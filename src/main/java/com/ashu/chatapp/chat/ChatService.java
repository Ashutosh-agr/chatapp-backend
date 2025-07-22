package com.ashu.chatapp.chat;

import com.ashu.chatapp.user.User;
import com.ashu.chatapp.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper mapper;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatByReceiverId(Authentication currentUser){

        final String userId = currentUser.getName();
        //System.out.println("Authenticated User ID: " + userId);
        return chatRepository.findChatByEmail(userId)
                .stream()
                .map(c -> mapper.toChatResponse(c,userId))
                .toList();
    }

    public String createChat(String senderId,String receiverId){
        Optional<Chat> existingChat = chatRepository.findChatsByReceiverAndSenderId(senderId,receiverId);

        if(existingChat.isPresent()){
            return existingChat.get().getId();
        }

        User sender = userRepository.findByPublicId(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + senderId + " not found"));

        User receiver = userRepository.findByPublicId(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("User with id: " + receiverId + " not found"));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setLastModifiedDate(LocalDateTime.now());
        chat.setRecipient(receiver);

        Chat savedChat = chatRepository.save(chat);

        return savedChat.getId();
    }
}
