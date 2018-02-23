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

    fun postSentAvocadoMessage(channel: String, sender: String, avocadosEach: Int, receivers: List<String>, remainingAvocados: Int) {
        Unirest
                .post("$host/api/chat.postEphemeral")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackEphemeralMessage(channel = channel,
                                text = craftAvocadoReceiptMessage(receivers, avocadosEach, remainingAvocados),
                                user = sender
                        )
                ))
                .asString()
    }

    fun craftAvocadoReceiptMessage(receivers: List<String>, avocadosEach: Int, remainingAvocados: Int): String {

        val receiversString = when(receivers.size) {
            1 -> receivers.first().asMention()
            2 -> receivers.joinToString(separator = " and ") { it.asMention() }
            else -> receivers.joinToString(
                    separator = ", ",
                    limit = receivers.size - 1,
                    truncated = "and ${receivers.last().asMention()}"
            ) { it.asMention() }
        }

        return "$receiversString ${if (receivers.size > 1) "each " else ""}" +
                "received $avocadosEach ${"avocado".pluralize(avocadosEach)} from you. " +
                "You have ${if (remainingAvocados == 0) "no" else "$remainingAvocados" } ${"avocado".pluralize(remainingAvocados)} " +
                "left to give out today."
    }

    private fun String.asMention(): String = "<@$this>"
    private fun String.pluralize(n: Int): String = if (n != 1) "${this}s" else this

    fun postNotEnoughAvocadosMessage(channel: String, sender: String, remainingAvocados: Int) {

        val message = when (remainingAvocados) {
            0 -> "You have no more avocados left to give out today!"
            else -> "You only have $remainingAvocados ${"avocado".pluralize(remainingAvocados)} left to give out today!"
        }

        Unirest
                .post("$host/api/chat.postEphemeral")
                .header("Authorization", "Bearer $botToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(jacksonObjectMapper().writeValueAsString(
                        SlackEphemeralMessage(channel = channel,
                                text = message,
                                user = sender
                        )
                ))
                .asString()
    }
}

data class SlackMessage(val channel: String, val text: String)
data class SlackEphemeralMessage(val channel: String, val text: String, val user: String)
