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
interface AvocadoReceiptRepository: MongoRepository<AvocadoReceipt, String> {
    fun findByEventId(eventId: String): List<AvocadoReceipt>
}


/**
 * Use this method to "safely" save a list of avocado receipts to the repository.
 * Prevents the repository from making modifications to the entity objects
 * @return the saved (possibly modified) list
 */
fun <ID> MongoRepository<AvocadoReceipt, ID>.saveAvocadoReceipts(entities: Iterable<AvocadoReceipt>): List<AvocadoReceipt> =
        saveAll(entities.map { it.copy() })