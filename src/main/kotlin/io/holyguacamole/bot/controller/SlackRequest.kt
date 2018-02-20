package io.holyguacamole.bot.controller

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.holyguacamole.bot.slack.SlackUser

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = UrlVerification::class, name = "url_verification"),
    JsonSubTypes.Type(value = EventCallback::class, name = "event_callback")
])
interface SlackRequest {
    val token: String
    val type: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventCallback(override val token: String,
                         override val type: String,
                         @JsonProperty("team_id") val teamId: String,
                         @JsonProperty("api_app_id") val apiAppId: String,
                         val event: Event,
                         @JsonProperty("authed_users") val authedUsers: List<String>,
                         @JsonProperty("event_id") val eventId: String,
                         @JsonProperty("event_time") val eventTime: Long) : SlackRequest

@JsonIgnoreProperties(ignoreUnknown = true)
data class UrlVerification(override val token: String,
                           override val type: String,
                           val challenge: String) : SlackRequest

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = MessageEvent::class, name = "message"),
    JsonSubTypes.Type(value = MessageEvent::class, name = "app_mention"),
    JsonSubTypes.Type(value = UserChangeEvent::class, name = "user_change")
])
interface Event {
    val type: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEvent(override val type: String,
                        val channel: String,
                        val user: String,
                        val text: String,
                        val ts: String,
                        val edited: MessageEventEdited? = null) : Event

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEventEdited(val user: String, val ts: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserChangeEvent(override val type: String,
                           val slackUser: SlackUser) : Event
