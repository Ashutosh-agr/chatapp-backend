package com.ashu.chatapp.message;

import com.ashu.chatapp.chat.Chat;
import com.ashu.chatapp.chat.ChatRepository;
import com.ashu.chatapp.file.FileService;
import com.ashu.chatapp.file.FileUtils;
import com.ashu.chatapp.file.SavedFile;
import com.ashu.chatapp.notification.Notification;
import com.ashu.chatapp.notification.NotificationService;
import com.ashu.chatapp.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveMessage(MessageRequest messageRequest) {
        Chat chat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat Not Found"));

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);
        message.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));

        messageRepository.save(message);

        // Send over WebSocket
        messagingTemplate.convertAndSendToUser(
                messageRequest.getReceiverId(),  // user destination
                "/queue/messages",               // destination suffix
                mapper.toMessageResponse(message)
        );

        // Send notification as well (if needed)
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(messageRequest.getType())
                .content(messageRequest.getContent())
                .senderId(messageRequest.getSenderId())  // fixed incorrect field
                .receiverId(messageRequest.getReceiverId())
                .notificationType(NotificationType.MESSAGE)
                .chatName(chat.getChatName(message.getSenderId()))
                .build();

        notificationService.sendNotification(messageRequest.getReceiverId(), notification);
    }

    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not Found"));

        final String recipientId = getRecipientId(chat, authentication);

        messageRepository.setMessagesToSeenByChatId(chatId, MessageState.SEEN);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(getSenderId(chat, authentication))
                .receiverId(recipientId)
                .notificationType(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(recipientId, notification);
    }

    // File: com/ashu/chatapp/message/MessageService.java

    // File: com/ashu/chatapp/message/MessageService.java

    public Message uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not Found"));

        String senderId = getSenderId(chat, authentication);
        String recipientId = getRecipientId(chat, authentication);

        SavedFile savedFile = fileService.saveFile(file, senderId);
        if (savedFile == null) throw new RuntimeException("File upload failed");

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(recipientId);
        String fileName = file.getOriginalFilename();
        MessageType type;
        if (fileName != null && fileName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            type = MessageType.IMAGE;
        } else {
            type = MessageType.FILE;
        }
        message.setType(type);
        message.setState(MessageState.SENT);
        message.setMediaPath(savedFile.getPath()); // used for internal file reading
        message.setMediaUrl(savedFile.getUrl());   // sent to frontend
        message.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
        message.setContent(fileName);

        messageRepository.save(message);

        messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/messages",
                mapper.toMessageResponse(message)
        );
        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/messages",
                mapper.toMessageResponse(message)
        );

        NotificationType notificationType = type == MessageType.IMAGE ? NotificationType.IMAGE : NotificationType.FILE;

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .notificationType(notificationType)
                .messageType(type)
                .senderId(senderId)
                .receiverId(recipientId)
                .media(FileUtils.readFileFromLocation(savedFile.getPath()))
                .build();

        notificationService.sendNotification(recipientId, notification);

        return message;
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        return chat.getSender().getId().equals(authentication.getName())
                ? chat.getSender().getId()
                : chat.getRecipient().getId();
    }

    private String getRecipientId(Chat chat, Authentication authentication) {
        return chat.getSender().getId().equals(authentication.getName())
                ? chat.getRecipient().getId()
                : chat.getSender().getId();
    }
}
