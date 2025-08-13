package bitc.full502.spring.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class RoomIdUtil {
    private RoomIdUtil() {}

    public static String directRoomId(String a, String b) {
        String left  = a.compareToIgnoreCase(b) <= 0 ? a : b;
        String right = a.compareToIgnoreCase(b) <= 0 ? b : a;
        String key = left + "#" + right; // 충돌 줄이려고 구분자 포함

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return toHex(digest).substring(0, 24); // 길이 줄여서 사용(24자)
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}