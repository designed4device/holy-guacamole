package io.holyguacamole.bot.message

import io.holyguacamole.bot.AVOCADO_TEXT
import io.holyguacamole.bot.controller.Event
import io.holyguacamole.bot.controller.EventCallback
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventService(val repository: AvocadoReceiptRepository, val chatService: ChatService) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun process(eventCallback: EventCallback): Boolean =
            when (eventCallback.event.type) {
                "app_mention" -> processAppMentionEvent(eventCallback.event)
                "message" -> processMessageEvent(eventCallback)
                else -> false
            }

    private fun processMessageEvent(eventCallback: EventCallback): Boolean {
        val mentions = eventCallback.event.findMentionedPeople()
        val count = eventCallback.event.countGuacamoleIngredients()
        if (count == 0 || mentions.isEmpty()) return false
        if (repository.findByEventId(eventCallback.eventId).isNotEmpty()) return false

        log.info("Avocado sent")

        repository.saveAll(
                mentions.flatMap { mention ->
                    mapUntil(count) {
                        AvocadoReceipt(
                                eventId = eventCallback.eventId,
                                sender = eventCallback.event.user,
                                receiver = mention,
                                timestamp = eventCallback.event.ts.toDouble().toLong())
                    }
                }
        )

        return true
    }

    private fun processAppMentionEvent(event: Event): Boolean {
        if (event.text.toLowerCase().contains("leaderboard")) {
            chatService.postLeaderboard(event.channel)
        }
        return true
    }
}

fun <T> mapUntil(end: Int, fn: () -> T): List<T> = (0 until end).map { fn() }

fun Event.countGuacamoleIngredients(): Int = this.text.split(AVOCADO_TEXT).size - 1
fun Event.findMentionedPeople(): List<String> = Regex("<@(U[0-9A-Z]*?)>")
        .findAll(this.text)
        .mapNotNull { it.groups[1]?.value }
        .filter { it != this.user }
        .toList()
