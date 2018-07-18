package dk.techtify.swipr.helper;

import java.util.UUID;

import dk.techtify.swipr.model.user.User;

/**
 * Created by graforlov on 11/15/2016.
 */

public class StringHelper {

    public static String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "")
                + String.valueOf(System.currentTimeMillis());
    }

    public static String getMyAddress() {
        User u = User.getLocalUser();
        if (u.getContactInfo() == null) {
            return "";
        }
        return u.getContactInfo().getStreet() + "\n" + u.getContactInfo().getCity() + "\n" + u.getContactInfo().getPostCode() + "\n" + u.getContactInfo().getMobile();
    }
}
