package dk.techtify.swipr.model.chat;

import java.io.Serializable;

import dk.techtify.swipr.model.profile.Follow;
import dk.techtify.swipr.model.store.SellerBuyer;

/**
 * Created by Pavel on 1/22/2017.
 */

public class Recipient extends Follow implements Serializable {

    public Recipient(SellerBuyer seller) {
        super(seller.getId(), seller.getName(), seller.getPhotoUrl());
    }

    public Recipient(ChatRoom chatRoom) {
        super(chatRoom.getUserId(), chatRoom.getUserName(), chatRoom.getUserPhotoUrl());
    }
}
