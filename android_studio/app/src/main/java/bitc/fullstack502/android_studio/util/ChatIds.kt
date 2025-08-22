package bitc.fullstack502.android_studio.util

import java.security.MessageDigest

object ChatIds {
    /** 두 userId를 정렬 → 해시 → 항상 같은 roomId 생성 (서버 RoomIdUtil 과 동일) */
    fun roomIdFor(a: String, b: String): String {
        val (left, right) =
            if (a.compareTo(b, ignoreCase = true) <= 0) a to b else b to a

        val key = "$left#$right"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))

        return digest.toHex().substring(0, 24) // 24자 substring
    }

    private fun ByteArray.toHex(): String {
        val sb = StringBuilder(size * 2)
        for (b in this) sb.append(String.format("%02x", b))
        return sb.toString()
    }
}
