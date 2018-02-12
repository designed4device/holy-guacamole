package io.holyguacamole.bot.controller

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.holyguacamole.bot.message.MessageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(@Value("\${slack.token}") val token: String, val service: MessageService) {

    private val log = LoggerFactory.getLogger(EventController::class.java)

    @PostMapping("/messages")
    fun message(@RequestBody request: SlackRequest): ResponseEntity<SlackResponse> =
            if (request.token != token) {
                log.error("Attempted with token: ${request.token}")
                ResponseEntity.status(401).build()
            } else {
                log.info(request.toString())
                when (request) {
                    is ChallengeRequest -> ResponseEntity.ok(ChallengeResponse(challenge = request.challenge) as SlackResponse)
                    is MessageEventRequest -> ResponseEntity.status(200).body(MessageResponse(service.process(request)) as SlackResponse)
                    else -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
                }
            }
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("event_callback")
data class MessageEventRequest(override val token: String,
                               override val type: String,
                               @JsonProperty("team_id") val teamId: String,
                               @JsonProperty("api_app_id") val apiAppId: String,
                               val event: MessageEvent,
                               @JsonProperty("authed_users") val authedUsers: List<String>,
                               @JsonProperty("event_id") val eventId: String,
                               @JsonProperty("event_time") val eventTime: Long) : SlackRequest

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEvent(val type: String,
                        val channel: String,
                        val user: String,
                        val text: String,
                        val ts: String,
                        val edited: MessageEventEdited? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEventEdited(val user: String, val ts: String)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("url_verification")
data class ChallengeRequest(override val token: String,
                            override val type: String,
                            val challenge: String) : SlackRequest

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = ChallengeRequest::class, name = "url_verification"),
    JsonSubTypes.Type(value = MessageEventRequest::class, name = "event_callback")
])
interface SlackRequest {
    val token: String
    val type: String
}

interface SlackResponse
data class ChallengeResponse(val challenge: String) : SlackResponse
data class MessageResponse(val succeeded: Boolean): SlackResponse
