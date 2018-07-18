package dk.techtify.swipr.model.push;

import dk.techtify.swipr.model.chat.MessageContent;
import dk.techtify.swipr.model.store.SellerBuyer;
import dk.techtify.swipr.model.user.User;

/**
 * Created by Pavel on 1/27/2017.
 */

public class OutgoingPush {

    public static final String HAS_SENT_MESS_EN = " has sent you a message";
    public static final String HAS_SENT_MESS_DK = " har sendt dig en besked";

    public static final String NEW_BID_EN = "New bid";
    public static final String NEW_BID_DK = "Du modtog nyt bud";

    public static final String BID_CANCELED_EN = "The bid has been cancelled";
    public static final String BID_CANCELED_DK = "Budet blev annulleret";

    public static final String BID_DECLINED_EN = "The bid has been declined";
    public static final String BID_DECLINED_DK = "Øv! Dit bud blev desværre ikke accepteret";

    public static final String BID_ACCEPTED_EN = "The bid has been accepted";
    public static final String BID_ACCEPTED_DK  = "Hurraaa! Dit bud blev accepteret";

    int type;
    String titleDk;
    String titleEn;
    String textDk;
    String textEn;
    String senderPhotoUrl;
    String senderName;
    String recipientId;

    public OutgoingPush(int type, SellerBuyer buyer) {
        this.type = type;
        titleEn = titleDk = User.getLocalUser().getName();
        if (type == MessageContent.TYPE_NEW_BID) {
            textEn = NEW_BID_EN;
            textDk = NEW_BID_DK;
        } else if (type == MessageContent.TYPE_BID_DECLINED) {
            textEn = BID_DECLINED_EN;
            textDk = BID_DECLINED_DK;
        } else if (type == MessageContent.TYPE_BID_ACCEPTED) {
            textEn = BID_ACCEPTED_EN;
            textDk = BID_ACCEPTED_DK;
        } else if (type == MessageContent.TYPE_BID_CANCELED) {
            textEn = BID_CANCELED_EN;
            textDk = BID_CANCELED_DK;
        }
        this.recipientId = buyer.getId();
    }

    public OutgoingPush(int type, String text,
                        String senderPhotoUrl, String senderName, String recipientId) {
        this.type = type;
        if (type == MessageContent.TYPE_PRODUCT_MESSAGE) {
            titleEn = senderName + HAS_SENT_MESS_EN;
            titleDk = senderName + HAS_SENT_MESS_DK;
            textDk = textEn = text;
        }
        this.senderPhotoUrl = senderPhotoUrl;
        this.senderName = senderName;
        this.recipientId = recipientId;
    }

    public int getType() {
        return type;
    }

    public String getTitleDk() {
        return titleDk;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getTextDk() {
        return textDk;
    }

    public String getTextEn() {
        return textEn;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRecipientId() {
        return recipientId;
    }
}
