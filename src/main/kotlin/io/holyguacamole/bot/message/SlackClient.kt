package io.holyguacamole.bot.message

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.Unirest
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackClient(@Value("\${slack.host}") val host: String,
                  @Value("\${slack.token.bot}") val botToken: String) {

    fun postMessage(channel: String, text: String = "", attachments: List<MessageAttachment> = emptyList()) {
        Unirest
                .post("$host/api/chat.postMessage")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackMessage(channel = channel,
                                text = text,
                                attachments = attachments
                        )
                ))
                .asString()
    }

    fun postEphemeralMessage(channel: String, user: String, text: String) {
        Unirest
                .post("$host/api/chat.postEphemeral")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackEphemeralMessage(channel, text, user)
                ))
                .asString()
    }

    fun getUserInfo(userId: String): SlackUser? {
        val response = Unirest.get("$host/api/users.info")
                .queryString("user", userId)
                .header("Authorization", "Bearer $botToken")
                .header("Accept", "application/json")
                .asString().body
        return jacksonObjectMapper().readValue(response, SlackUserResponse::class.java).user
    }

    fun sendDirectMessage(user: String, text: String, attachments: List<MessageAttachment>) {
        postMessage(channel = openConversationChannel(user), text = text, attachments = attachments)
    }

    private fun openConversationChannel(user: String): String {
        val response = Unirest
                .post("$host/api/conversations.open")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackOpenConversationRequest(users = user)
                ))
                .asString().body
        return jacksonObjectMapper().readValue(response, SlackOpenConversationResponse::class.java).channel?.id!!
    }
}

data class SlackOpenConversationRequest(val users: String)
data class SlackMessage(val channel: String, val text: String, val attachments: List<MessageAttachment> = emptyList())
data class SlackEphemeralMessage(val channel: String, val text: String, val user: String)

data class MessageAttachment(
        val title: String,
        val pretext: String,
        val text: String,
        @JsonIgnore val markdownIn: List<MARKDOWN>
) {
    val mrkdwn_in = markdownIn.map { it.value }
}

enum class MARKDOWN(val value:String) {
    TEXT("text"),
    PRETEXT("pretext")
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackOpenConversationResponse(val ok: String, val channel: Channel?, val error: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Channel(val id: String)
