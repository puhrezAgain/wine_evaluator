package com.wine_evaluator.wine_evaluator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WineEvaluatorApplication

fun main(args: Array<String>) {
	runApplication<WineEvaluatorApplication>(*args)
}
