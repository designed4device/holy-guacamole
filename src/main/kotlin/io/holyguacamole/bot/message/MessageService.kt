package io.holyguacamole.bot.message

import io.holyguacamole.bot.AVOCADO_TEXT
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.MessageEventRequest
import org.springframework.stereotype.Service

@Service
class MessageService(val repository: AvocadoReceiptRepository) {

    fun process(messageEvent: MessageEventRequest): Boolean {
        val mentions = messageEvent.event.findMentionedPeople()
        if (!messageEvent.event.hasGuacamoleIngredient() && mentions.isEmpty()) return false

        repository.saveAll(listOf(AvocadoReceipt(
                eventId = messageEvent.event_id,
                sender = messageEvent.event.user,
                receiver = mentions.first(),
                timestamp = messageEvent.event.ts.toDouble().toLong())))

        return true
    }
}

fun MessageEvent.hasGuacamoleIngredient(): Boolean = this.text.contains(AVOCADO_TEXT)
fun MessageEvent.findMentionedPeople(): List<String> = Regex("<@(U[0-9A-Z]*?)>")
        .findAll(this.text)
        .mapNotNull { it.groups[1]?.value }
        .toList()
