package app.sizzle.bank

import org.junit.Assert.assertThrows
import org.junit.Test

class UpdatePolicyTest {
    private val hash = "a".repeat(64)
    private val path = "/dozyFJ/Sizzle-/${"a".repeat(40)}/downloads/Sizzle-0.3.0.apk"
    @Test fun acceptsImmutableSizzleDownload() {
        UpdatePolicy.validateDownload("https://raw.githubusercontent.com$path", hash)
    }
    @Test fun rejectsUnsafeSources() {
        listOf("http://raw.githubusercontent.com$path", "https://evil.example$path", "https://raw.githubusercontent.com.evil.example$path", "https://raw.githubusercontent.com$path?x=1", "https://raw.githubusercontent.com:443$path", "https://raw.githubusercontent.com" + path.replace("Sizzle-", "Other-"), "https://raw.githubusercontent.com" + path.replace("a".repeat(40), "main")).forEach { url ->
            assertThrows(IllegalArgumentException::class.java) { UpdatePolicy.validateDownload(url, hash) }
        }
    }
    @Test fun rejectsInvalidChecksums() {
        listOf("", "a".repeat(63), "z".repeat(64)).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) { UpdatePolicy.validateDownload("https://raw.githubusercontent.com$path", invalid) }
        }
    }
}
