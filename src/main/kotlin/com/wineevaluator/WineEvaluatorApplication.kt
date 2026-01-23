package com.wineevaluator

import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableAsync
class WineEvaluatorApplication

fun main(args: Array<String>) {
	runApplication<WineEvaluatorApplication>(*args)
}
