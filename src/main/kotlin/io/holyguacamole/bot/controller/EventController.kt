package io.holyguacamole.bot.controller

import io.holyguacamole.bot.message.EventService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.CommonsRequestLoggingFilter

@RestController
class EventController(@Value("\${slack.token.verification}") val token: String, val service: EventService) {

    private val log = LoggerFactory.getLogger(EventController::class.java)

    @PostMapping("/events")
    fun message(@RequestBody request: SlackRequest): ResponseEntity<UrlVerificationResponse> =
            if (request.token != token) {
                log.error("Attempted with token: ${request.token}")
                ResponseEntity.status(401).build()
            } else {
                log.info(request.toString())
                when (request) {
                    is UrlVerification -> ResponseEntity.ok(UrlVerificationResponse(challenge = request.challenge))
                    is EventCallback -> {
                        service.process(request)
                        ResponseEntity.status(200).build()
                    }
                    else -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
                }
            }

}


@Configuration
class LoggingFilterConfig {
    @Bean
    fun logFilter(): CommonsRequestLoggingFilter  =
            CommonsRequestLoggingFilter().apply {
                setIncludeQueryString(true)
                setIncludePayload(true)
                setMaxPayloadLength(10000)
                isIncludeHeaders = true
                setAfterMessagePrefix("REQUEST DATA: ")
            }
}
