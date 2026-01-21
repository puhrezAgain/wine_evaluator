package com.wine_evaluator.wine_evaluator.upload

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant

@Component
class LocalStorage: Storage {
    private val baseDir = File("uploads").absoluteFile

    init {
        baseDir.mkdirs()
    }

    override fun store(uploadId: String, file: MultipartFile): String {
        val dir = File(baseDir, uploadId).apply { mkdirs() }
        val target = File(dir, file.originalFilename ?: "upload")
        file.inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return target.absolutePath
    }
}