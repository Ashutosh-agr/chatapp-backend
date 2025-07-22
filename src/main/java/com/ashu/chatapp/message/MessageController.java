package com.ashu.chatapp.message;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message")
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveMessage(@RequestBody MessageRequest messageRequest){
        messageService.saveMessage(messageRequest);
    }

    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MessageResponse> uploadMedia(
            @RequestParam("chat-id") String chatId,

            @Parameter()

            @RequestParam("file")MultipartFile file,
            Authentication authentication
    ){
        Message message = messageService.uploadMediaMessage(chatId,file,authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toMessageResponse(message));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setMessagesToSeen(@RequestParam("chat-id") String chatId,Authentication authentication){
        messageService.setMessagesToSeen(chatId,authentication);
    }

    @GetMapping("/chat/{chat-id}")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable("chat-id") String chatId){
        return ResponseEntity.ok(messageService.findChatMessages(chatId));
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(MessageTypingEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.getReceiverId(),
                "/queue/messages",
                event
        );
    }
}
