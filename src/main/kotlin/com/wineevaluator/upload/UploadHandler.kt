package com.wineevaluator.upload

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.time.Instant
import com.wineevaluator.document.DocumentProcessingPipeline
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.upload.queue.DocumentProcessingQueue
import com.wineevaluator.upload.storage.UploadStorage
import com.wineevaluator.common.value.UploadId

@Service
class UploadHandler(
    private val storage: UploadStorage,
    private val parseQueue: DocumentProcessingQueue,
    private val pipeline: DocumentProcessingPipeline
) {
    fun handleUpload(file: MultipartFile): DocumentFile {
        val upload = storage.store(file)

        // parseQueue.enqueue(uploadId)
        pipeline.process(upload)
        return upload
    }
}