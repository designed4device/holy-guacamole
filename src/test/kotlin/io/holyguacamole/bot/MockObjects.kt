package io.holyguacamole.bot

import io.holyguacamole.bot.MockChannels.directMessage
import io.holyguacamole.bot.MockChannels.general
import io.holyguacamole.bot.MockIds.appbot
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.Timestamp.today
import io.holyguacamole.bot.Timestamp.todayPlusOneHour
import io.holyguacamole.bot.Timestamp.yesterday
import io.holyguacamole.bot.controller.UrlVerification

import io.holyguacamole.bot.controller.EventCallback
import io.holyguacamole.bot.controller.EventCallbackType.APP_MENTION
import io.holyguacamole.bot.controller.EventCallbackType.MEMBER_JOINED_CHANNEL
import io.holyguacamole.bot.controller.EventCallbackType.MESSAGE
import io.holyguacamole.bot.controller.EventCallbackType.USER_CHANGE
import io.holyguacamole.bot.controller.JoinedChannelEvent
import io.holyguacamole.bot.controller.Message
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.RequestType.EVENT_CALLBACK
import io.holyguacamole.bot.controller.RequestType.URL_VERIFICATION
import io.holyguacamole.bot.controller.UserChangeEvent
import io.holyguacamole.bot.message.AvocadoReceipt
import io.holyguacamole.bot.message.ContentCrafter.AVOCADO_TEXT
import io.holyguacamole.bot.message.ContentCrafter.TACO_TEXT
import io.holyguacamole.bot.message.toTimestamp
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import io.holyguacamole.bot.user.User
import java.time.LocalDateTime
import java.time.temporal.ChronoField


private val token = "thisisagoodtoken"

object MockIds {
    val mark = "U00000Z01"
    val jeremy = "U00000Z02"
    val patrick = "U00000Z03"
    val appbot = "U00000APP"
}

object MockChannels {
    val general = "C0000000005"
    val directMessage = "D0000001"
}

object MockUrlVerification {

    val withCorrectToken = UrlVerification(
            token = token,
            challenge = "somechallenge",
            type = URL_VERIFICATION
    )
    val withIncorrectToken = UrlVerification(
            token = "verybadtoken",
            challenge = "somechallenge",
            type = URL_VERIFICATION
    )
}

object MockMessages {

    val withSingleMentionAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndMultipleAvocados = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withMultipleMentionsAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withMultipleMentionsAndMultipleAvocados = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = jeremy,
                    text = "<@$mark> <@$patrick> you're the best $AVOCADO_TEXT $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionWithoutAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = mark,
                    text = "<@$patrick> you're the best",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withAvocadoWithoutMention = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = jeremy,
                    text = "$AVOCADO_TEXT is the best",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withoutMentionAndAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = patrick,
                    text = "I'm the best!",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndSingleAvocadoFromThemself = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = mark,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndSingleAvocadoFromBot = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = appbot,
                    text = "<@$mark> you're the best $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withBotMentionAndSingleAvocado = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = MESSAGE,
                    channel = general,
                    user = mark,
                    text = "<@$appbot> derp $AVOCADO_TEXT",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionAndSingleTaco = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $TACO_TEXT",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withSingleMentionSingleAvocadoAndSingleTaco = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "<@$mark> you're the best $AVOCADO_TEXT $TACO_TEXT",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withNoMentionAndSingleTaco = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = general,
                    user = patrick,
                    text = "you're the best $TACO_TEXT",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosYesterday = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    subtype = "message_deleted",
                    channel = general,
                    ts = todayPlusOneHour,
                    previousMessage = Message(
                            type = withMultipleMentionsAndMultipleAvocados.event.type,
                            user = (withMultipleMentionsAndMultipleAvocados.event as MessageEvent).user!!,
                            text = (withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!,
                            ts = yesterday
                    )
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosToday = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    subtype = "message_deleted",
                    channel = general,
                    ts = todayPlusOneHour,
                    previousMessage = Message(
                            type = withMultipleMentionsAndMultipleAvocados.event.type,
                            user = (withMultipleMentionsAndMultipleAvocados.event as MessageEvent).user!!,
                            text = (withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!,
                            ts = today
                    )
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
}

object MockDirectMessages {
    val avocados = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = directMessage,
                    user = patrick,
                    text = "avocados",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val leaderboard = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = directMessage,
                    user = patrick,
                    text = "leaderboard",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withNoCommand = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = directMessage,
                    user = patrick,
                    text = "how are you?",
                    ts = today
            ),
            type = "event_callback",
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val withHelp =  EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = "message",
                    channel = directMessage,
                    user = patrick,
                    text = "help",
                    ts = today
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
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val jeremyToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = patrick,
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val jeremyToMark = AvocadoReceipt(
            eventId = "12345678",
            sender = jeremy,
            receiver = mark,
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val markToPatrick = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = patrick,
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val markToJeremy = AvocadoReceipt(
            eventId = "12345678",
            sender = mark,
            receiver = jeremy,
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val patrickToJeremy = AvocadoReceipt(
            eventId = "12345678",
            sender = patrick,
            receiver = jeremy,
            message = "test message",
            timestamp = today.toTimestamp()
    )
    val singleMentionAndSingleAvocadoReceipts =
            listOf(patrickToMark.copy(
                    message = (MockMessages.withSingleMentionAndSingleAvocado.event as MessageEvent).text!!
            ))
    val singleMentionAndMultipleAvocadosReceipts = listOf(
            patrickToMark.copy(
                    message = (MockMessages.withSingleMentionAndMultipleAvocados.event as MessageEvent).text!!
            ),
            patrickToMark.copy(
                    message = (MockMessages.withSingleMentionAndMultipleAvocados.event as MessageEvent).text!!
            ))
    val multipleMentionsAndSingleAvocadoReceipts = listOf(
            jeremyToMark.copy(
                    message = (MockMessages.withMultipleMentionsAndSingleAvocado.event as MessageEvent).text!!
            ),
            jeremyToPatrick.copy(
                    message = (MockMessages.withMultipleMentionsAndSingleAvocado.event as MessageEvent).text!!
            ))
    val multipleMentionsAndMultipleAvocadosReceipts = listOf(
            jeremyToMark.copy(
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToMark.copy(
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToPatrick.copy(
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToPatrick.copy(
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ))
    val multipleMentionsAndMultipleAvocadosReceiptsYesterday = listOf(
            jeremyToMark.copy(
                    timestamp = yesterday.toTimestamp(),
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToMark.copy(
                    timestamp = yesterday.toTimestamp(),
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToPatrick.copy(
                    timestamp = yesterday.toTimestamp(),
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            ),
            jeremyToPatrick.copy(
                    timestamp = yesterday.toTimestamp(),
                    message = (MockMessages.withMultipleMentionsAndMultipleAvocados.event as MessageEvent).text!!
            )
    )

}

object MockAppMentions {
    val leaderboard = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = APP_MENTION,
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> leaderboard",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345677",
            eventTime = 1234567890
    )
    val leaderboard1 = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = APP_MENTION,
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> leaderboard 1",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val leaderboard12 = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = APP_MENTION,
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> leaderboard 12",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345679",
            eventTime = 1234567890
    )
    val help = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = APP_MENTION,
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> help",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )

    val unknownCommand = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = MessageEvent(
                    type = APP_MENTION,
                    channel = general,
                    user = jeremy,
                    text = "<@$appbot> unknownCommand",
                    ts = today
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )
}


object MockJoinedChannelEvents {
    val botJoined = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = JoinedChannelEvent(
                    type = MEMBER_JOINED_CHANNEL,
                    channel = general,
                    user = appbot
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
    )
    val markJoined = EventCallback(
            token = token,
            teamId = "abc",
            apiAppId = "123",
            event = JoinedChannelEvent(
                    type = MEMBER_JOINED_CHANNEL,
                    channel = general,
                    user = mark
            ),
            type = EVENT_CALLBACK,
            authedUsers = listOf(appbot),
            eventId = "12345678",
            eventTime = 1234567890
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
                    type = USER_CHANGE,
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
            type = EVENT_CALLBACK,
            authedUsers = listOf("U123556"),
            eventId = "12345678",
            eventTime = 1234567890
    )
}

object Empty {
    val messageEvent = MessageEvent(type = "", channel = "", user = "", text = "", ts = "")
    val eventCallback = EventCallback(
            token = "",
            type = "",
            teamId = "",
            apiAppId = "",
            event = messageEvent,
            authedUsers = emptyList(),
            eventId = "",
            eventTime = 0L
    )
}

object Timestamp {
    val today = LocalDateTime.now().toTimestampString()
    val todayPlusOneHour = LocalDateTime.now().plusHours(1).toTimestampString()
    val yesterday = LocalDateTime.now().minusDays(1).toTimestampString()
}

fun LocalDateTime.toTimestampString(): String = "${this.toEpochSecond(java.time.ZoneOffset.UTC)}.${this.getLong(ChronoField.MICRO_OF_SECOND)}"
