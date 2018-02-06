package io.holyguacamole.bot.message

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

data class AvocadoReceipt(
        val id: String? = null,
        val eventId: String,
        val sender: String,
        val receiver: String,
        val timestamp: Long
)

@Repository
interface AvocadoReceiptRepository: MongoRepository<AvocadoReceipt, String>
