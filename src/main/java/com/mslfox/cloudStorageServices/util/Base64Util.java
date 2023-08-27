package com.mslfox.cloudStorageServices.util;
import java.util.Base64;
import java.util.Random;

public class Base64Util {

    public static String encode(String originalString) {
        return Base64.getEncoder().encodeToString(originalString.getBytes());
    }
        public static String decode(String encodedString) {
        return new String(Base64.getDecoder().decode(encodedString));
    }
    public static String generateRandomLinkKey() {
        byte[] randomBytes = new byte[16];
        new Random().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
