package com.futurenbetter.saas.common.utils;

import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class PasswordUtils {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}<>?";
    private static final String ALL = LOWER + UPPER + DIGIT + SPECIAL;

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword() {
        int length = 10;

        StringBuilder password = new StringBuilder(length);

        password.append(randomChar(LOWER));
        password.append(randomChar(UPPER));
        password.append(randomChar(DIGIT));
        password.append(randomChar(SPECIAL));

        for (int i = 4; i < length; i++) {
            password.append(randomChar(ALL));
        }

        return shuffle(password.toString());
    }

    private static char randomChar(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}