package bitc.fullstack502.android_studio.util

fun fullUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http", ignoreCase = true)) return path
    val p = if (path.startsWith("/")) path else "/$path"
    return "http://10.0.2.2:8080$p"
}
