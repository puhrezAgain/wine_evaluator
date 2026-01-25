package com.wineevaluator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class WineEvaluatorApplication

fun main(args: Array<String>) {
    runApplication<WineEvaluatorApplication>(*args)
}
