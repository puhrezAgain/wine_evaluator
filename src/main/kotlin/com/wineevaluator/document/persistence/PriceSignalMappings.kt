package com.wineevaluator.document.persistence

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.PriceSignal

internal fun DocumentPriceSignalEntity.toDomain(): PriceSignal =
    PriceSignal(
        uploadId = UploadId(uploadId),
        tokens = identityTokens,
        prices = priceHints,
        rawLine = rawRow,
    )

internal fun PriceSignal.toEntity(): DocumentPriceSignalEntity =
    DocumentPriceSignalEntity(
        uploadId = uploadId.value,
        identityTokens = tokens,
        priceHints = prices,
        rawRow = rawLine,
    )
