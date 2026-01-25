package com.wineevaluator.document.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(
    name = "document_price_signal",
    indexes = [
        Index(
            name = "idx_price_signal_upload_id",
            columnList = "upload_id",
        ),
    ],
)
class DocumentPriceSignalEntity(
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    val id: UUID? = null,
    @Column(nullable = false)
    val uploadId: UUID,
    @ElementCollection
    @CollectionTable(
        name = "document_price_signal_token",
        joinColumns = [JoinColumn(name = "signal_id")],
        indexes = [
            Index(
                name = "idx_signal_token_token_signal",
                columnList = "token, signal_id",
            ),
        ],
    )
    @Column(name = "token", nullable = false)
    val identityTokens: Set<String>,
    @ElementCollection
    @CollectionTable(
        name = "document_price_signal_price",
        joinColumns = [JoinColumn(name = "signal_id")],
    )
    @Column(name = "price", nullable = false)
    val priceHints: List<Int>,
    @Lob
    @Column(nullable = false)
    val rawRow: String,
)
