package com.wineevaluator.upload.storage

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream
import org.springframework.stereotype.Component

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
    ): DocumentFile =
            copyAndCreateDocument(
                    input,
                    uploadId,
                    filename,
                    File(baseDir, uploadId.value.toString())
                            .apply { mkdirs() }
                            .let { File(it, filename) }
                            .toPath()
            )

    override fun storeTemp(
            input: InputStream,
            uploadId: UploadId,
            filename: String,
    ): DocumentFile =
            copyAndCreateDocument(
                    input,
                    uploadId,
                    filename,
                    createTempFile(prefix = "wine-diagnostic-", suffix = "-" + filename).also {
                        it.toFile().deleteOnExit()
                    }
            )

    private fun copyAndCreateDocument(
            input: InputStream,
            uploadId: UploadId,
            filename: String,
            target: Path,
    ): DocumentFile {
        input.use { target.outputStream().use { output -> it.copyTo(output) } }
        return DocumentFile(uploadId, filename, target, Instant.now())
    }
}
