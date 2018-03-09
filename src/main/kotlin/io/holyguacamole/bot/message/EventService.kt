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
import io.holyguacamole.bot.message.ContentCrafter.TACO_TEXT
import io.holyguacamole.bot.message.ContentCrafter.helpMessage
import io.holyguacamole.bot.message.ContentCrafter.notEnoughAvocados
import io.holyguacamole.bot.message.ContentCrafter.receivedAvocadoMessage
import io.holyguacamole.bot.message.ContentCrafter.sentAvocadoMessage
import io.holyguacamole.bot.message.ContentCrafter.welcomeMessage
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

    @Async
    fun process(eventCallback: EventCallback) {
        if (eventAlreadyProcessed(eventCallback.eventId)) return

        when (eventCallback.event.type) {
            APP_MENTION -> processAppMentionEvent(eventCallback.event as MessageEvent)
            MESSAGE -> processMessageEvent(eventCallback.eventId, eventCallback.event as MessageEvent)
            USER_CHANGE -> processUserChangeEvent((eventCallback.event as UserChangeEvent).user)
            MEMBER_JOINED_CHANNEL -> processMemberJoinedChannelEvent(eventCallback.event as JoinedChannelEvent)
        }
    }

    private fun eventAlreadyProcessed(eventId: String): Boolean =
            if (processedEvents.contains(eventId)) {
                true
            } else {
                processedEvents.add(eventId)
                if (processedEvents.size > 100) processedEvents.removeAt(0)
                false
            }

    private fun processMessageEvent(eventId: String, event: MessageEvent): Boolean {

        if (event.previousMessage != null && event.previousMessage.ts.toTimestamp().isToday()) {
            when (event.subtype) {
                MESSAGE_DELETED -> repository.deleteBySenderAndTimestamp(
                        sender = event.previousMessage.user,
                        timestamp = event.previousMessage.ts.toTimestamp()
                )
            }
            return true
        }
        if (event.user == null) return false

        val mentions = event.findMentionedPeople()
        val avocadosInMessage = event.countGuacamoleIngredients()

        if (mentions.isNotEmpty() && avocadosInMessage == 0 && event.tacoCheck()) {
            slackClient.postEphemeralMessage(
                    channel = event.channel,
                    user = event.user,
                    text = AVOCADO_REMINDER
            )
            return false
        }
        if (avocadosInMessage == 0 || mentions.isEmpty()) return false

        val sender = userService.findByUserIdOrGetFromSlack(event.user)
        if (sender == null || sender.isBot) return false

        val avocadosSentToday = repository.findBySenderToday(sender.userId).size
        if ((avocadosSentToday + (avocadosInMessage * mentions.size)) > 5) {
            val remainingAvocados = 5 - avocadosSentToday
            slackClient.postEphemeralMessage(event.channel, event.user, notEnoughAvocados(remainingAvocados))
            return false
        }

        if (repository.findByEventId(eventId).isNotEmpty()) return false

        mentions.filter {
            userService.findByUserIdOrGetFromSlack(it)?.isBot == false
        }.flatMap { mention ->
            mapUntil(avocadosInMessage) {
                AvocadoReceipt(
                        eventId = eventId,
                        sender = event.user,
                        receiver = mention,
                        timestamp = event.ts.toTimestamp()
                )
            }
        }.executeIfNotEmpty {
            it.save()
            sendReceiptMessage(event.channel, event.user, avocadosSentToday, it)
        }
        return true
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
            slackClient.sendDirectMessage(it, receivedAvocadoMessage(avocadosEach, sender))
        }
    }

    private fun processAppMentionEvent(event: MessageEvent): Boolean {
        val text = event.text?.toLowerCase() ?: ""
        when {
            text.contains("leaderboard") -> slackClient.postMessage(
                    channel = event.channel,
                    text = craftLeaderboardMessage(repository.getLeaderboard(10)))
            text.contains("help") -> slackClient.postMessage(
                    channel = event.channel,
                    attachments = helpMessage
            )
        }

        if (event.text?.toLowerCase()?.contains("leaderboard") == true) {

        }
        return true
    }

    private fun craftLeaderboardMessage(avocadoCounts: List<AvocadoCount>): String =
            avocadoCounts.joinToString(separator = "\n") {
                val user = userService.findByUserIdOrGetFromSlack(it.receiver)?.name ?: it.receiver
                "$user: ${it.count}"
            }

    private fun processUserChangeEvent(slackUser: SlackUser): Boolean {
        userService.replace(slackUser.toUser())
        return true
    }

    private fun processMemberJoinedChannelEvent(event: JoinedChannelEvent): Boolean {
        if (event.user == bot) {
            slackClient.postMessage(
                    channel = event.channel,
                    attachments = listOf(welcomeMessage)
            )
        }
        return true
    }
}

fun <T> mapUntil(end: Int, fn: () -> T): List<T> = (0 until end).map { fn() }

fun MessageEvent.countGuacamoleIngredients(): Int = (this.text?.split(AVOCADO_TEXT)?.size ?: 1) - 1
fun MessageEvent.findMentionedPeople(): List<String> = Regex("<@([0-9A-Z]*?)>")
        .findAll(this.text ?: "")
        .mapNotNull { it.groups[1]?.value }
        .filter { it != this.user }
        .toList()

fun MessageEvent.tacoCheck(): Boolean = this.text?.contains(TACO_TEXT) ?: false

fun String.toTimestamp(): Long = this.toDouble().toLong()
fun Long.isToday(): Boolean = LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC).isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT))