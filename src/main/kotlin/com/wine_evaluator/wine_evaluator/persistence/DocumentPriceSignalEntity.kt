package com.wine_evaluator.wine_evaluator.persistence

import org.hibernate.annotations.UuidGenerator
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "document_price_signal")
class DocumentPriceSignalEntity(

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    val id:  UUID? = null,

    @Column(nullable = false)
    val uploadId: UUID,

    @ElementCollection
    @CollectionTable(
        name = "document_price_signal_token",
        joinColumns = [JoinColumn(name = "signal_id")]
    )
    @Column(name = "token", nullable = false)
    val identityTokens: Set<String>,

    @ElementCollection
    @CollectionTable(
        name = "document_price_signal_price",
        joinColumns = [JoinColumn(name = "signal_id")]
    )
    @Column(name = "price", nullable = false)
    val priceHints: List<Int>,

    @Lob
    @Column(nullable = false)
    val rawRow: String
)