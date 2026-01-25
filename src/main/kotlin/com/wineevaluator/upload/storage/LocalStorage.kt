package com.wineevaluator.upload.storage

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream
import java.time.Instant

@Component
class LocalStorage : UploadStorage {
    private val baseDir = File("uploads").absoluteFile

    init {
        baseDir.mkdirs()
    }

    override fun store(
        input: InputStream,
        uploadId: UploadId,
        filename: String,
    ): DocumentFile {
        val target =
            File(baseDir, uploadId.value.toString())
                .apply { mkdirs() }
                .let { File(it, filename) }

        input.use {
            target.outputStream().use { output ->
                it.copyTo(output)
            }
        }

        return DocumentFile(
            id = uploadId,
            filename = target.name,
            path = target.toPath(),
            uploadedAt = Instant.now(),
        )
    }
}
