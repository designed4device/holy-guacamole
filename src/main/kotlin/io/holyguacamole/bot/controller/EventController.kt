package io.holyguacamole.bot.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(@Value("\${slack.token}") val token: String) {

    @PostMapping("/messages")
    fun messages(@RequestBody request: ChallengeRequest): ResponseEntity<ChallengeResponse> =
            if (request.token != token) {
                ResponseEntity.status(401).build()
            } else {
                ResponseEntity.ok(ChallengeResponse(challenge = request.challenge))
            }
}

data class ChallengeResponse(val challenge: String)

data class ChallengeRequest(val challenge: String, val token: String, val type: String)
