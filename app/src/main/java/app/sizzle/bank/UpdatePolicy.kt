package app.sizzle.bank

import java.net.URI

internal object UpdatePolicy {
    fun validateDownload(url: String, sha256: String) {
        val uri = URI(url)
        require(uri.scheme == "https" && uri.host == "raw.githubusercontent.com" && uri.port == -1 && uri.userInfo == null && uri.query == null && uri.fragment == null)
        require(Regex("/dozyFJ/Sizzle-/[a-f0-9]{40}/downloads/Sizzle-[0-9.]+\\.apk").matches(uri.path))
        require(Regex("[a-f0-9]{64}").matches(sha256))
    }
}
