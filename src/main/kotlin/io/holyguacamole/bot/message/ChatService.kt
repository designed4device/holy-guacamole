package io.holyguacamole.bot.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.Unirest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class ChatService(@Value("\${slack.host}") val host: String,
                  @Value("\${slack.token.bot}") val botToken: String,
                  @Autowired private val repository: AvocadoReceiptRepository) {


    fun postLeaderboard(channel: String) {
        Unirest
                .post("$host/api/chat.postMessage")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackMessage(channel = channel, text = getLeaderboard()))
                )
                .asString()
    }

    private fun getLeaderboard(): String =
            repository.getLeaderboard()
                    .joinToString(separator = "\n") {
                        "${it.receiver}: ${it.count}"
                    }
}

data class SlackMessage(val channel: String, val text: String)
