package com.ashu.chatapp.chat;

import com.ashu.chatapp.common.BaseAuditingEntity;
import com.ashu.chatapp.message.Message;
import com.ashu.chatapp.message.MessageState;
import com.ashu.chatapp.message.MessageType;
import com.ashu.chatapp.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat")

@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID,
        query = "SELECT distinct c from Chat as c where c.sender.id = :senderId or c.recipient.id = :senderId order by createdDate desc")

@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_EMAIL,
query = "SELECT distinct c from Chat as c where c.sender.email = :email or c.recipient.email = :email order by createdDate desc")

@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_RECEIVER_ID,
        query = "SELECT distinct c from Chat as c where (c.sender.id = :senderId and c.recipient.id = :recipientId) or (c.sender.id = :recipientId and c.recipient.id = :senderId) order by createdDate desc")


public class Chat extends BaseAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    @OneToMany(mappedBy = "chat",fetch = FetchType.EAGER)
    @OrderBy("createdDate DESC")
    private List<Message> message;

    @Transient
    public String getChatName(final String senderId){
        if(recipient.getId().equals(senderId)){
            return sender.getFirstName() + " " + sender.getLastName();
        }
        return recipient.getFirstName() + " " + recipient.getLastName();
    }

    @Transient
    public long getUnreadMessages(final String senderId){
        return message
                .stream()
                .filter(m -> m.getReceiverId().equals(senderId))
                .filter(m -> MessageState.SENT == m.getState())
                .count();
    }

    @Transient
    public String getLastMessage(){
        if(message != null && !message.isEmpty()) {
            if (message.get(0).getType() != MessageType.TEXT) {
                return "Attachment";
            }
            return message.get(0).getContent();
        }
        return null;
    }

    @Transient
    public LocalDateTime getLastMessageTime(){
        if (message != null && !message.isEmpty()) {
            return message.get(0).getChat().getCreatedDate();
        }
        return null;
    }
}
