package com.wineevaluator.upload.storage

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import java.nio.file.Path
import java.util.UUID
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile

@Component
class LocalStorage: UploadStorage {
    private val baseDir = File("uploads").absoluteFile

    init {
        baseDir.mkdirs()
    }

    override fun store(file: MultipartFile): DocumentFile {
        val uploadId = UploadId(UUID.randomUUID())

        val dir = File(baseDir, uploadId.value.toString()).apply { mkdirs() }
        val target = File(dir, file.originalFilename ?: "upload")

        file.inputStream.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return DocumentFile(
            id = uploadId,
            filename = file.originalFilename ?: "upload",
            path = target.toPath(),
            uploadedAt = Instant.now()
        )
    }
}