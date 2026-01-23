package com.wineevaluator.document.persistence

import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId

fun DocumentPriceSignalEntity.toDomain(): PriceSignal =
    PriceSignal(
        uploadId = UploadId(uploadId),
        tokens = identityTokens,
        prices = priceHints,
        rawLine = rawRow,
    )

fun PriceSignal.toEntity(): DocumentPriceSignalEntity =
    DocumentPriceSignalEntity(
        uploadId = uploadId.value,
        identityTokens = tokens,
        priceHints = prices,
        rawRow = rawLine
    )