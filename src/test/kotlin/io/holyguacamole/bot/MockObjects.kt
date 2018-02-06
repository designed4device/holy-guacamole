package io.holyguacamole.bot

import io.holyguacamole.bot.controller.ChallengeRequest
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.MessageEventRequest
import io.holyguacamole.bot.message.AvocadoReceipt

object MockMessages {

    val challenge = ChallengeRequest (
            token = "thisisagoodtoken",
            challenge = "somechallenge",
            type = "url_verification"
    )
    val withSingleMentionAndAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = "U12356",
                    text = "<@U0LAN0Z89> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withMultipleMentionsAndAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = "U12356",
                    text = "<@U0LAN0Z89> <@U0LAN0Z10> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withSingleMentionWithoutAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = "U12356",
                    text = "<@U0LAN0Z89> you're the best",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )

    val withAvocadoWithoutMention = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = "U12356",
                    text = "$AVOCADO_TEXT is the best",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )

    val withoutMentionAndAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = "U12356",
                    text = "I'm the best!",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
}

object MockAvocadoReceipts {
    val receipt = AvocadoReceipt(
            eventId = "12345678",
            sender = "U12356",
            receiver = "U0LAN0Z89",
            timestamp = 1355517523
    )
    val persistedReceipt = receipt.copy(id = "abcdefg")
}
