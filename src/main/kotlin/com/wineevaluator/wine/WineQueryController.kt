package com.wineevaluator.wine

import org.springframework.web.bind.annotation.*
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse

@RestController
@RequestMapping("/wine")
class WineQueryController(
    private val handler: WineQueryHandler
){

    @PostMapping("/query")
    fun query(@RequestBody request: WineQueryRequest): WineQueryResponse {
        val matches = handler.query(request.wine, request.price.toInt())

        return WineQueryResponse(
            original = request.wine,
            queryPrice = request.price,
            matches = matches)
    }
}