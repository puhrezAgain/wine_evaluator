package com.wine_evaluator.wine_evaluator.winequery

import org.springframework.web.bind.annotation.*
import com.wine_evaluator.wine_evaluator.interpretation.WineTokenizer

@RestController
@RequestMapping("/wine")
class WineQueryController{

    @PostMapping("/query")
    fun query(@RequestBody request: WineQueryRequest): WineQueryResponse {
        val tokens = request.wine
            .let(WineTokenizer::tokenize)
            .let(WineTokenizer::toIdentitySet)

        return WineQueryResponse(request.wine, request.price, tokens)
    }
}