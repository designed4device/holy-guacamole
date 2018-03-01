package io.holyguacamole.bot.controller

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.holyguacamole.bot.controller.EventCallbackType.APP_MENTION
import io.holyguacamole.bot.controller.EventCallbackType.MEMBER_JOINED_CHANNEL
import io.holyguacamole.bot.controller.EventCallbackType.MESSAGE
import io.holyguacamole.bot.controller.EventCallbackType.USER_CHANGE
import io.holyguacamole.bot.controller.RequestType.EVENT_CALLBACK
import io.holyguacamole.bot.controller.RequestType.URL_VERIFICATION
import io.holyguacamole.bot.slack.SlackUser

object RequestType {
    const val URL_VERIFICATION = "url_verification"
    const val EVENT_CALLBACK = "event_callback"
}

object EventCallbackType {
    const val APP_MENTION = "app_mention"
    const val MESSAGE = "message"
    const val USER_CHANGE = "user_change"
    const val MEMBER_JOINED_CHANNEL = "member_joined_channel"
}

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = UrlVerification::class, name = URL_VERIFICATION),
    JsonSubTypes.Type(value = EventCallback::class, name = EVENT_CALLBACK)
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
    JsonSubTypes.Type(value = MessageEvent::class, name = MESSAGE),
    JsonSubTypes.Type(value = MessageEvent::class, name = APP_MENTION),
    JsonSubTypes.Type(value = UserChangeEvent::class, name = USER_CHANGE),
    JsonSubTypes.Type(value = JoinedChannelEvent::class, name = MEMBER_JOINED_CHANNEL)
])
interface Event {
    val type: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEvent(override val type: String,
                        val channel: String,
                        val user: String = "",
                        val text: String? = null,
                        val ts: String,
                        val edited: MessageEventEdited? = null) : Event

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageEventEdited(val user: String, val ts: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserChangeEvent(override val type: String,
                           val user: SlackUser) : Event

@JsonIgnoreProperties(ignoreUnknown = true)
data class JoinedChannelEvent(override val type: String, val channel: String, val user: String) : Event
