package io.holyguacamole.bot

import io.holyguacamole.bot.controller.ChallengeRequest

import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.MessageEventRequest
import io.holyguacamole.bot.message.AvocadoReceipt

private val mark = "U00000Z01"
private val jeremy = "U00000Z02"
private val patrick = "U00000Z03"

object MockMessages {

    val challenge = ChallengeRequest (
            token = "thisisagoodtoken",
            challenge = "somechallenge",
            type = "url_verification"
    )
    val withSingleMentionAndSingleAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withSingleMentionAndMultipleAvocados = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withMultipleMentionsAndSingleAvocado = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withMultipleMentionsAndMultipleAvocados = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
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
                    user = mark,
                    text = "<@$patrick> you're the best",
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
                    user = jeremy,
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
                    user = patrick,
                    text = "I'm the best!",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
    val withSingleMentionAndSingleAvocadoFromThemself = MessageEventRequest(
            token = "thisisagoodtoken",
            team_id = "abc",
            api_app_id = "123",
            event = MessageEvent(
                    type = "message",
                    channel = "C2147483705",
                    user = mark,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authed_users = listOf("U123556"),
            event_id = "12345678",
            event_time = 1234567890
    )
}

object MockAvocadoReceipts {
    private val patrickToMark = AvocadoReceipt(
            eventId = "12345678",
            sender = patrick,
            receiver = mark,
            timestamp = 1355517523
    )
    private val jeremyToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = patrick,
            timestamp = 1355517523
    )
    private val jeremyToMark = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = mark,
            timestamp = 1355517523
    )
    private val markToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = patrick,
            timestamp = 1355517523
    )
    private val markToJeremy = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = jeremy,
            timestamp = 1355517523
    )
    private val patrickToJeremy = AvocadoReceipt(
            eventId = "12345678",
            sender = patrick,
            receiver = jeremy,
            timestamp = 1355517523
    )
    val singleMentionAndSingleAvocadoReceipts = listOf(patrickToMark)
    val singleMentionAndMultipleAvocadosReceipts = listOf(patrickToMark, patrickToMark)
    val multipleMentionsAndSingleAvocadoReceipts = listOf(jeremyToMark, jeremyToPatrick)
    val multipleMentionsAndMultipleAvocadosReceipts = listOf(jeremyToMark, jeremyToMark, jeremyToPatrick, jeremyToPatrick)

}
