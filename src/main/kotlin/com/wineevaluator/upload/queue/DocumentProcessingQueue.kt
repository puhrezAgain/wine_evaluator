package com.wineevaluator.upload.queue

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DocumentProcessingQueue {
    fun enqueue(uploadId: UUID) {
        // TODO: push to Pub/Sub / SQS / DB table
        println("Enqueued parse job for upload $uploadId")
    }
}