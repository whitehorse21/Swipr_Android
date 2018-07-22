package dk.techtify.swipr.model.chat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pavel on 1/22/2017.
 */

public class ChatRoom {

    @Exclude
    String userId;
    String userName;
    String userPhotoUrl;
    String messageSenderId;
    String messageText;
    boolean messageSeen;
    long messageCreated;

    public ChatRoom(String userName, String userPhotoUrl, Message message, boolean messageSeen) {
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.messageSenderId = message.getSenderId();
        this.messageText = message.getText();
        this.messageSeen = messageSeen;
    }

    public ChatRoom(String key, Map<String, Object> map) {
        this.userId = key;
        this.userName = map.containsKey("userName") ? map.get("userName").toString() : "";
        this.userPhotoUrl = map.containsKey("userPhotoUrl") ? map.get("userPhotoUrl").toString() : null;
        this.messageSenderId = map.containsKey("messageSenderId") ? map.get("messageSenderId").toString() : "";
        this.messageText = map.containsKey("messageText") ? map.get("messageText").toString() : "";
        this.messageSeen = map.containsKey("messageSeen") ? (Boolean) map.get("messageSeen") : true;
        this.messageCreated = map.containsKey("messageCreated") ? (Long) map.get("messageCreated") : 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userName", userName);
        if (userPhotoUrl != null) {
            map.put("userPhotoUrl", userPhotoUrl);
        }
        map.put("messageSenderId", messageSenderId);
        map.put("messageText", messageText.length() > 64 ? messageText.substring(0, 61) + "..." : messageText);
        map.put("messageSeen", messageSeen);
        map.put("messageCreated", ServerValue.TIMESTAMP);
        return map;
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public String getMessageText() {
        return messageText;
    }

    @Exclude
    public boolean isMessageSeen() {
        return messageSeen;
    }

    public long getMessageCreated() {
        return messageCreated;
    }

    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessageSeen(boolean messageSeen) {
        this.messageSeen = messageSeen;
    }

    public void setMessageCreated(long messageCreated) {
        this.messageCreated = messageCreated;
    }
}
