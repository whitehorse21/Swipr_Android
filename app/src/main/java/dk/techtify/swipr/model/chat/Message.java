package dk.techtify.swipr.model.chat;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel on 1/21/2017.
 */

public class Message {

    String id;
    String text;
    String senderId;
    long created;
    MessageContent messageContent;

    public Message(String text, String senderId) {
        this.text = text;
        this.senderId = senderId;
        this.created = System.currentTimeMillis();
    }

    public Message(String text, String senderId, MessageContent messageContent) {
        this.text = text;
        this.senderId = senderId;
        this.created = System.currentTimeMillis();
        try {
            this.messageContent = (MessageContent) messageContent.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Message(Object key, Map<String, Object> map) {
        id = key.toString();
        text = map.containsKey("text") ? map.get("text").toString() : "";
        senderId = map.containsKey("senderId") ? map.get("senderId").toString() : "";
        created = map.containsKey("created") ? (Long) map.get("created") : 0;
        if (map.containsKey("content")) {
            messageContent = new MessageContent((Map<String, Object>) map.get("content"));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("senderId", senderId);
        map.put("created", ServerValue.TIMESTAMP);
        if (messageContent != null) {
            map.put("content", messageContent);
        }
        return map;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Message && ((Message) o).getId().equals(id);
    }
}
