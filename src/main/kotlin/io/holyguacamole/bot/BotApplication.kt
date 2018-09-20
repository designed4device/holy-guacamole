package io.holyguacamole.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class BotApplication

fun main(args: Array<String>) {
    runApplication<BotApplication>(*args)
}
