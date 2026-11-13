package app.sizzle.bank

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

internal data class AppUpdate(val code: Long, val name: String, val url: String, val sha256: String)

private fun connection(url: String): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
    connectTimeout = 15000
    readTimeout = 30000
    instanceFollowRedirects = false
    setRequestProperty("User-Agent", "Sizzle-Android")
}

private fun checkUpdate(installed: Long): AppUpdate? {
    val connection = connection("https://raw.githubusercontent.com/dozyFJ/Sizzle-/main/updates/latest.json")
    try {
        check(connection.responseCode == 200)
        val bytes = connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (output.size() <= 16384) {
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        require(bytes.size <= 16384)
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        val update = AppUpdate(json.getLong("versionCode"), json.getString("versionName"), json.getString("downloadUrl"), json.getString("sha256"))
        UpdatePolicy.validateDownload(update.url, update.sha256)
        require(update.name.length in 1..40)
        return update.takeIf { it.code > installed }
    } finally { connection.disconnect() }
}

@Suppress("DEPRECATION")
private fun versionCode(info: android.content.pm.PackageInfo): Long = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()

@Suppress("DEPRECATION")
private fun signer(info: android.content.pm.PackageInfo): Set<String> {
    val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
    return signatures.orEmpty().map { signature -> MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString("") { "%02x".format(it) } }.toSet()
}

@Suppress("DEPRECATION")
private fun downloadUpdate(context: android.content.Context, update: AppUpdate): File {
    UpdatePolicy.validateDownload(update.url, update.sha256)
    val directory = File(context.cacheDir, "updates").apply { mkdirs() }
    val partial = File(directory, "download.apk")
    val apk = File(directory, "sizzle-update.apk")
    apk.delete()
    val connection = connection(update.url)
    try {
        check(connection.responseCode == 200)
        val hash = MessageDigest.getInstance("SHA-256")
        var size = 0L
        connection.inputStream.use { input -> partial.outputStream().use { output ->
            val buffer = ByteArray(32768)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                size += count
                require(size <= 100 * 1024 * 1024)
                hash.update(buffer, 0, count)
                output.write(buffer, 0, count)
            }
        } }
        require(hash.digest().joinToString("") { "%02x".format(it) } == update.sha256)
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = requireNotNull(context.packageManager.getPackageArchiveInfo(partial.absolutePath, flags))
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        require(archive.packageName == context.packageName && versionCode(archive) == update.code && update.code > versionCode(installed))
        require(signer(archive).isNotEmpty() && signer(archive) == signer(installed))
        check(partial.renameTo(apk))
        return apk
    } finally { connection.disconnect(); partial.delete() }
}

@Composable
internal fun UpdateControl() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    var apk by remember { mutableStateOf<File?>(null) }
    fun install() {
        val file = apk ?: return
        if (!file.exists()) { apk = null; status = "Файл видалено. Завантажте оновлення ще раз."; return }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
            context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (_: Exception) { status = "Не вдалося відкрити встановлення. Спробуйте ще раз." }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) install()
        else status = "Дозвольте встановлення для Sizzle, щоб продовжити."
    }
    suspend fun check(manual: Boolean) {
        apk = null
        update = null
        busy = true
        try {
            val installed = versionCode(context.packageManager.getPackageInfo(context.packageName, 0))
            update = withContext(Dispatchers.IO) { checkUpdate(installed) }
            status = if (update == null) "У вас актуальна версія Sizzle." else "Доступна версія ${update!!.name}. Завантажити та встановити?"
            if (manual || update != null) dialog = true
        } catch (_: Exception) {
            status = "Не вдалося перевірити оновлення. Перевірте інтернет і спробуйте ще раз."
            if (manual) dialog = true
        } finally { busy = false }
    }
    LaunchedEffect(Unit) { check(false) }
    AssistChip(onClick = { if (!busy) { dialog = true; scope.launch { check(true) } } }, label = { Text(if (busy) "Перевірка…" else "Оновлення") })
    if (dialog) AlertDialog(onDismissRequest = { if (!busy) dialog = false }, title = { Text("Оновлення Sizzle") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (busy && apk == null) "Зачекайте, перевіряємо або завантажуємо оновлення…" else status)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (update != null) Text("APK із GitHub Sizzle. Перед встановленням перевіряються контрольна сума та підпис.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = {
        TextButton(enabled = !busy, onClick = {
            if (apk != null) {
                if (context.packageManager.canRequestPackageInstalls()) install()
                else {
                    status = "Дозвольте встановлення для Sizzle у налаштуваннях Android."
                    try { permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))) }
                    catch (_: Exception) { status = "Відкрийте налаштування Android та дозвольте Sizzle встановлювати оновлення." }
                }
            } else if (update != null) {
                val available = update!!
                busy = true
                scope.launch {
                    try {
                        apk = withContext(Dispatchers.IO) { downloadUpdate(context, available) }
                        status = "APK перевірено. Натисніть «Встановити», щоб відкрити встановлення Android."
                    } catch (_: Exception) { status = "Не вдалося завантажити або перевірити APK. Спробуйте ще раз." }
                    finally { busy = false }
                }
            } else dialog = false
        }) { Text(if (apk != null) "Встановити" else if (update != null) "Завантажити" else "Закрити") }
    }, dismissButton = { if (update != null) TextButton(enabled = !busy, onClick = { dialog = false }) { Text("Пізніше") } })
}
