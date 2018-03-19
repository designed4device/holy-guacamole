package io.holyguacamole.bot.message

import io.holyguacamole.bot.controller.EventCallback
import io.holyguacamole.bot.controller.EventCallbackSubtype.MESSAGE_DELETED
import io.holyguacamole.bot.controller.EventCallbackType.APP_MENTION
import io.holyguacamole.bot.controller.EventCallbackType.MEMBER_JOINED_CHANNEL
import io.holyguacamole.bot.controller.EventCallbackType.MESSAGE
import io.holyguacamole.bot.controller.EventCallbackType.USER_CHANGE
import io.holyguacamole.bot.controller.JoinedChannelEvent
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.UserChangeEvent
import io.holyguacamole.bot.message.ContentCrafter.AVOCADO_REMINDER
import io.holyguacamole.bot.message.ContentCrafter.AVOCADO_TEXT
import io.holyguacamole.bot.message.ContentCrafter.GUACWARD_MESSAGE
import io.holyguacamole.bot.message.ContentCrafter.TACO_TEXT
import io.holyguacamole.bot.message.ContentCrafter.avocadosLeft
import io.holyguacamole.bot.message.ContentCrafter.commandsMessage
import io.holyguacamole.bot.message.ContentCrafter.helpMessage
import io.holyguacamole.bot.message.ContentCrafter.notEnoughAvocados
import io.holyguacamole.bot.message.ContentCrafter.receivedAvocadoMessage
import io.holyguacamole.bot.message.ContentCrafter.revokedAvocadoMessageForReceiver
import io.holyguacamole.bot.message.ContentCrafter.revokedAvocadoMessageForSender
import io.holyguacamole.bot.message.ContentCrafter.sentAvocadoMessage
import io.holyguacamole.bot.message.ContentCrafter.welcomeMessage
import io.holyguacamole.bot.message.EventService.BotCommands.AVOCADO_COMMAND
import io.holyguacamole.bot.message.EventService.BotCommands.LEADERBOARD_COMMAND
import io.holyguacamole.bot.message.EventService.BotCommands.HELP_COMMAND
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.toUser
import io.holyguacamole.bot.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Service
class EventService(
        val repository: AvocadoReceiptRepository,
        val slackClient: SlackClient,
        val userService: UserService,
        @Value("\${bot.userId}") val bot: String
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val processedEvents = mutableListOf<String>()

    object BotCommands {
        const val AVOCADO_COMMAND = "avocados"
        const val LEADERBOARD_COMMAND = "leaderboard"
        const val HELP_COMMAND = "help"
    }

    @Async
    fun process(eventCallback: EventCallback) {
        //don't process the same event more than once
        if (processedEvents.contains(eventCallback.eventId)) return

        processedEvents.add(eventCallback.eventId)
        if (processedEvents.size > 100) processedEvents.removeAt(0)

        when (eventCallback.event.type) {
            APP_MENTION -> processAppMentionEvent(eventCallback.event as MessageEvent)
            MESSAGE -> processMessageEvent(eventCallback.eventId, eventCallback.event as MessageEvent)
            USER_CHANGE -> processUserChangeEvent((eventCallback.event as UserChangeEvent).user)
            MEMBER_JOINED_CHANNEL -> processMemberJoinedChannelEvent(eventCallback.event as JoinedChannelEvent)
        }
    }

    private fun processAppMentionEvent(event: MessageEvent) {
        event.text?.toLowerCase()?.let { text ->
            when {
                text.contains(Regex("$LEADERBOARD_COMMAND \\d*")) -> slackClient.postMessage(
                        channel = event.channel,
                        text = craftLeaderboardMessage(repository.getLeaderboard(
                                Regex("$LEADERBOARD_COMMAND (\\d*)").find(text)?.groupValues?.get(1)?.toLong() ?: 10))
                )
                text.contains(LEADERBOARD_COMMAND) -> slackClient.postMessage(
                        channel = event.channel,
                        text = craftLeaderboardMessage(repository.getLeaderboard())
                )
                text.contains("help") -> slackClient.postMessage(
                        channel = event.channel,
                        attachments = helpMessage
                )
                else -> slackClient.postMessage(
                        channel = event.channel,
                        attachments = listOf(commandsMessage.copy(title = "", pretext = GUACWARD_MESSAGE))
                )
            }
        }
    }

    private fun sendLeaderboard(channel: String, limit: Long = 10) {
        slackClient.postMessage(
                channel = channel,
                text = craftLeaderboardMessage(repository.getLeaderboard(limit))
        )
    }

    private fun processMessageEvent(eventId: String, event: MessageEvent) {
        when {
            event.previousMessage != null -> processEditedOrDeletedMessage(event)
            event.isDirectMessageToBot() -> processDirectMessage(event)
            event.user == null || event.text == null -> return
            tacoCheck(event.text, event.user) -> sendAvocadoReminder(event.user, event.channel)
            else -> processAvocadoMessage(event.user, event.text, event.channel, event.ts.toTimestamp(), eventId)
        }
    }

    private fun processUserChangeEvent(slackUser: SlackUser) {
        userService.replace(slackUser.toUser())
    }

    private fun processMemberJoinedChannelEvent(event: JoinedChannelEvent) {
        if (event.user == bot) {
            slackClient.postMessage(
                    channel = event.channel,
                    attachments = listOf(welcomeMessage)
            )
        }
    }

    private fun processDirectMessage(event: MessageEvent) {
        if (event.user == null) return

        event.text?.toLowerCase()?.let { text ->
            when {
                text == HELP_COMMAND -> slackClient.postMessage(channel = event.channel, attachments = helpMessage)
                text == AVOCADO_COMMAND -> slackClient.postMessage(event.channel, avocadosLeft(calculateRemainingAvocados(event.user)))
                text.contains(Regex("$LEADERBOARD_COMMAND \\d*")) -> sendLeaderboard(
                        channel = event.channel,
                        limit = Regex("$LEADERBOARD_COMMAND (\\d*)").find(text)?.groupValues?.get(1)?.toLong() ?: 10
                )
                text.contains(LEADERBOARD_COMMAND) -> sendLeaderboard(event.channel)
            }
        }
    }

    private fun processEditedOrDeletedMessage(event: MessageEvent) {
        if (event.previousMessage != null && event.previousMessage.ts.toTimestamp().isToday()) {
            when (event.subtype) {
                MESSAGE_DELETED -> repository.revokeAvocadosBySenderAndTimestamp(
                        sender = event.previousMessage.user,
                        timestamp = event.previousMessage.ts.toTimestamp()
                ).executeIfNotEmpty {
                    val remainingAvocados = 5 - repository.findBySenderToday(event.previousMessage.user).size
                    slackClient.postEphemeralMessage(
                            channel = event.channel,
                            user = event.previousMessage.user,
                            text = revokedAvocadoMessageForSender(
                                    revokedAvocadosPerMention = it.first().count,
                                    mentions = it.map { it.receiver },
                                    remainingAvocados = remainingAvocados
                            )
                    )
                    it.forEach {
                        slackClient.sendDirectMessage(
                                user = it.receiver,
                                attachments = listOf(MessageAttachment(
                                        title = "",
                                        pretext = revokedAvocadoMessageForReceiver(
                                                sender = event.previousMessage.user,
                                                avocadosRevoked = it.count,
                                                channel = event.channel
                                        ),
                                        text = event.previousMessage.text,
                                        markdownIn = listOf(MARKDOWN.TEXT, MARKDOWN.PRETEXT)
                                ))
                        )
                    }
                }
            }
        }
    }

    private fun processAvocadoMessage(user: String, text: String, channel: String, timestamp: Long, eventId: String) {
        val mentions = findMentionedPeople(text, user)
        val avocadosInMessage = countGuacamoleIngredients(text)
        if (avocadosInMessage == 0 || mentions.isEmpty()) return

        val sender = userService.findByUserIdOrGetFromSlack(user)
        if (sender == null || sender.isBot) return

        val avocadosSentToday = repository.findBySenderToday(user).size
        val remainingAvocados = calculateRemainingAvocados(user, avocadosSentToday)

        if ((avocadosSentToday + (avocadosInMessage * mentions.size)) > 5) {
            slackClient.postEphemeralMessage(channel, user, notEnoughAvocados(remainingAvocados))
            return
        }

        mentions.filter {
            userService.findByUserIdOrGetFromSlack(it)?.isBot == false
        }.flatMap { mention ->
            mapUntil(avocadosInMessage) {
                AvocadoReceipt(
                        eventId = eventId,
                        sender = user,
                        receiver = mention,
                        message = text,
                        timestamp = timestamp
                )
            }
        }.executeIfNotEmpty {
            it.save()
            sendReceiptMessage(channel, user, avocadosSentToday, it)
        }
    }

    private fun sendAvocadoReminder(user: String, channel: String) =
            slackClient.postEphemeralMessage(
                    channel = channel,
                    user = user,
                    text = AVOCADO_REMINDER
            )

    private fun sendReceiptMessage(channel: String, sender: String, avocadosSentToday: Int, avocadoReceipts: List<AvocadoReceipt>) {

        val uniqueReceivers = avocadoReceipts.map { it.receiver }.distinct()
        val avocadosEach = avocadoReceipts.size / uniqueReceivers.size
        val remainingAvocados = 5 - avocadosSentToday - uniqueReceivers.size * avocadosEach

        slackClient.postEphemeralMessage(
                channel = channel,
                user = sender,
                text = sentAvocadoMessage(uniqueReceivers, avocadosEach, remainingAvocados)
        )

        uniqueReceivers.map {
            slackClient.sendDirectMessage(
                    user = it,
                    text = receivedAvocadoMessage(avocadosEach, sender, channel),
                    attachments = listOf(MessageAttachment(
                            title = "",
                            pretext = "",
                            text = avocadoReceipts.first().message,
                            markdownIn = listOf(MARKDOWN.TEXT)
                    ))
            )
        }
    }

    private fun tacoCheck(text: String, user: String): Boolean =
            findMentionedPeople(text, user).isNotEmpty()
                    && countGuacamoleIngredients(text) == 0
                    && text.contains(TACO_TEXT)

    private fun calculateRemainingAvocados(userId: String, sentAvocados: Int? = null): Int =
            5 - (sentAvocados ?: repository.findBySenderToday(userId).size)

    fun MessageEvent.isDirectMessageToBot(): Boolean = this.channel.startsWith("D")

    private fun craftLeaderboardMessage(avocadoCounts: List<AvocadoCount>): String =
            avocadoCounts.joinToString(separator = "\n") {
                val user = userService.findByUserIdOrGetFromSlack(it.receiver)?.name ?: it.receiver
                "$user: ${it.count}"
            }

    private fun <T> List<T>.executeIfNotEmpty(fn: (List<T>) -> Unit): List<T> {
        if (this.isNotEmpty()) fn(this)
        return this
    }

    private fun List<AvocadoReceipt>.save() {
        if (this.isNotEmpty()) {
            repository.saveAll(this)
            log.info("Avocado sent")
        }
    }

    companion object {
        fun countGuacamoleIngredients(text: String): Int = (text.split(AVOCADO_TEXT).size) - 1
        fun findMentionedPeople(text: String, user: String): List<String> = Regex("<@([0-9A-Z]*?)>")
                .findAll(text)
                .mapNotNull { it.groups[1]?.value }
                .filter { it != user }
                .toList()


    }
}

fun <T> mapUntil(end: Int, fn: () -> T): List<T> = (0 until end).map { fn() }
fun String.toTimestamp(): Long = this.toDouble().toLong()
fun Long.isToday(): Boolean = LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC).isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT))