package io.holyguacamole.bot.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.Unirest
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserResponse
import io.holyguacamole.bot.user.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SlackClient(@Value("\${slack.host}") val host: String,
                  @Value("\${slack.token.bot}") val botToken: String) {

    fun getUserInfo(userId: String): SlackUser {
        val response = Unirest.get("$host/api/users.info")
                .queryString("user", userId)
                .header("Authorization", "Bearer $botToken")
                .header("Accept", "application/json")
                .asString().body
        return jacksonObjectMapper().readValue(response, SlackUserResponse::class.java).user
    }

    fun postLeaderboard(channel: String, map: Map<User, Int>) {
        Unirest
                .post("$host/api/chat.postMessage")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackMessage(channel = channel,
                                text = map.toList().joinToString("\n") { "${it.first.name}: ${it.second}" }
                        )
                ))
                .asString()
    }
}

data class SlackMessage(val channel: String, val text: String)
