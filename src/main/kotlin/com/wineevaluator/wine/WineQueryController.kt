package com.wineevaluator.wine

import org.springframework.web.bind.annotation.*
import com.wineevaluator.document.interpretation.tokenize
import com.wineevaluator.document.interpretation.toIdentitySet
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse

@RestController
@RequestMapping("/wine")
class WineQueryController(
    private val handler: WineQueryHandler
){

    @PostMapping("/query")
    fun query(@RequestBody request: WineQueryRequest): WineQueryResponse {
        val tokens = request.wine
            .let(::tokenize)
            .let(::toIdentitySet)
        val matches = handler.query(tokens, request.price.toInt())

        return WineQueryResponse(
            original = request.wine,
            queryPrice = request.price,
            matches = matches)
    }
}