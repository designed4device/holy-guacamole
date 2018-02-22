package io.holyguacamole.bot

import io.holyguacamole.bot.MockChannels.general
import io.holyguacamole.bot.MockIds.appbot
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.controller.UrlVerification

import io.holyguacamole.bot.controller.EventCallback
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.UserChangeEvent
import io.holyguacamole.bot.message.AvocadoCount
import io.holyguacamole.bot.message.AvocadoReceipt
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import io.holyguacamole.bot.user.User


private val token = "thisisagoodtoken"


object MockIds {
    val mark = "U00000Z01"
    val jeremy = "U00000Z02"
    val patrick = "U00000Z03"
    val appbot = "U00000APP"
}

object MockChannels {
    val general = "C0000000005"
}

object MockUrlVerification {

    val withCorrectToken = UrlVerification(
            token = token,
            challenge = "somechallenge",
            type = "url_verification"
    )
    val withIncorrectToken = UrlVerification(
            token = "verybadtoken",
            challenge = "somechallenge",
            type = "url_verification"
    )
}

object MockMessages {

    val withSingleMentionAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndMultipleAvocados = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withMultipleMentionsAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withMultipleMentionsAndMultipleAvocados = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionWithoutAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = mark,
                    text = "<@$patrick> you're the best",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withAvocadoWithoutMention = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = jeremy,
                    text = "$AVOCADO_TEXT is the best",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withoutMentionAndAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "I'm the best!",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndSingleAvocadoFromThemself = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = mark,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndSingleAvocadoFromBot = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = appbot,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withBotMentionAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = mark,
                    text = "<@$appbot> derp $AVOCADO_TEXT",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
}

object MockAvocadoReceipts {
    val patrickToMark = AvocadoReceipt(
            eventId = "12345678",
            sender = patrick,
            receiver = mark,
            timestamp = 1355517523
    )
    val jeremyToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = patrick,
            timestamp = 1355517523
    )
    val jeremyToMark = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = mark,
            timestamp = 1355517523
    )
    val markToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = patrick,
            timestamp = 1355517523
    )
    val markToJeremy = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = jeremy,
            timestamp = 1355517523
    )
    val patrickToJeremy = AvocadoReceipt(
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

object MockAppMentions {
    val leaderboard = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "app_mention",
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> leaderboard",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val showMeTheLeaderboard = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "app_mention",
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> show me the LEADERBOARD",
                    ts = "1355517523.000005"
            ),
            type = "event_callback",
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )
}

object MockLeaderboards {
    val patrick3jeremy2mark1 = listOf(
            AvocadoCount(patrick, 3),
            AvocadoCount(jeremy, 2),
            AvocadoCount(mark, 1)
    )
}

object MockUsers {
    val markardito = User(userId = mark, name = "markardito", isBot = false)
    val eightRib = markardito.copy(name = "8rib")
    val jeremyskywalker = User(userId = jeremy, name = "jeremyskywalker", isBot = false)
    val feeneyfeeneybobeeney = User(userId = patrick, name = "feeneyfeeneybobeeney", isBot = false)
    val holyguacamole = User(userId = appbot, name = "HolyGuacamole", isBot = true)
}

object MockSlackUsers {
    val jeremySlack = SlackUser(
            id = jeremy,
            name = "jeremy_kapler",
            profile = SlackUserProfile(
                    realName = "Jeremy Kapler",
                    displayName = "jeremyskywalker"
            ),
            isBot = false,
            isRestricted = false,
            isUltraRestricted = false
    )
}

object MockUserChangeEvent {
    val markNameUpdate = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = UserChangeEvent(
                    type = "user_change",
                    user = SlackUser(
                            id = mark,
                            name = "markardito",
                            profile = SlackUserProfile(
                                    realName = "Mark Ardito",
                                    displayName = "8rib"
                            ),
                            isBot = false,
                            isRestricted = false,
                            isUltraRestricted = false
                    )
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
}
