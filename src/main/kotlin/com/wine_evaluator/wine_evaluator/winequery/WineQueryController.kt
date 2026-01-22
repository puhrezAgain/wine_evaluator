package com.wine_evaluator.wine_evaluator.winequery

import org.springframework.web.bind.annotation.*
import com.wine_evaluator.wine_evaluator.interpretation.WineTokenizer

@RestController
@RequestMapping("/wine")
class WineQueryController(
    private val service: WineQueryService
){

    @PostMapping("/query")
    fun query(@RequestBody request: WineQueryRequest): WineQueryResponse {
        val tokens = request.wine
            .let(WineTokenizer::tokenize)
            .let(WineTokenizer::toIdentitySet)
        val matches = service.query(tokens, request.price.toInt())

        return WineQueryResponse(
            original = request.wine,
            queryPrice = request.price,
            matches = matches)
    }
}