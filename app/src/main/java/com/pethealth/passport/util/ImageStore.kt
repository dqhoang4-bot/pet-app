package com.pethealth.passport.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class ImageStore(private val context: Context) {

    fun copyToInternal(uriString: String): String? {
        if (uriString.startsWith("file://") || uriString.startsWith(context.filesDir.path)) {
            return uriString.removePrefix("file://")
        }
        return runCatching {
            val uri = Uri.parse(uriString)
            val dir = File(context.filesDir, "images").apply { mkdirs() }
            val file = File(dir, "img_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        }.getOrNull()
    }
}
