package com.wine_evaluator.wine_evaluator.upload

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ParseQueue {
    fun enqueue(uploadId: UUID) {
        // TODO: push to Pub/Sub / SQS / DB table
        println("Enqueued parse job for upload $uploadId")
    }
}