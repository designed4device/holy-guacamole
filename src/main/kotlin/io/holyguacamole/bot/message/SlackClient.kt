package io.holyguacamole.bot.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.Unirest
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackClient(@Value("\${slack.host}") val host: String,
                  @Value("\${slack.token.bot}") val botToken: String) {

    fun getUserInfo(userId: String): SlackUser? {
        val response = Unirest.get("$host/api/users.info")
                .queryString("user", userId)
                .header("Authorization", "Bearer $botToken")
                .header("Accept", "application/json")
                .asString().body
        return jacksonObjectMapper().readValue(response, SlackUserResponse::class.java).user
    }

    fun postLeaderboard(channel: String, map: Map<String, Int>) {
        Unirest
                .post("$host/api/chat.postMessage")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackMessage(channel = channel,
                                text = map.toList().joinToString("\n") { "${it.first}: ${it.second}" }
                        )
                ))
                .asString()
    }

    fun postSentAvocadoMessage(channel: String, user: String) {
        val message = "You sent an avocado."

        Unirest
                .post("$host/api/chat.postEphemeral")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackEphemeralMessage(channel = channel,
                                text = message,
                                user = user
                        )
                ))
                .asString()
    }
}

data class SlackMessage(val channel: String, val text: String)
data class SlackEphemeralMessage(val channel: String, val text: String, val user: String)
