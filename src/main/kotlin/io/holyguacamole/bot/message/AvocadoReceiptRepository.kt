package io.holyguacamole.bot.message

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class AvocadoReceiptRepository(
        private val mongoRepository: AvocadoReceiptMongoRepository,
        private val template: MongoTemplate) {

    fun findByEventId(eventId: String): List<AvocadoReceipt> = mongoRepository.findByEventId(eventId)

    fun findAll(): List<AvocadoReceipt> = mongoRepository.findAll()

    fun deleteAll() = mongoRepository.deleteAll()

    fun saveAll(entities: Iterable<AvocadoReceipt>): List<AvocadoReceipt> =
            mongoRepository.saveAll(entities.map { it.copy() })

    fun getLeaderboard(): List<AvocadoCount> =
            template.aggregate(
                    Aggregation.newAggregation(
                            Aggregation.group("receiver")
                                    .count().`as`("count"),
                            Aggregation.sort(Sort.Direction.DESC, "count"),
                            Aggregation.project("receiver", "count")
                    ),
                    AvocadoReceipt::class.java,
                    AvocadoCount::class.java
            ).map { it }
}

@Repository
interface AvocadoReceiptMongoRepository : MongoRepository<AvocadoReceipt, String> {
    fun findByEventId(eventId: String): List<AvocadoReceipt>
}

data class AvocadoReceipt(
        val id: String? = null,
        val eventId: String,
        val sender: String,
        val receiver: String,
        val timestamp: Long
)

data class AvocadoCount(
        @Id
        val receiver: String,
        val count: Int
)

//
///**
// * Use this method to "safely" save a list of avocado receipts to the repository.
// * Prevents the repository from making modifications to the entity objects
// * @return the saved (possibly modified) list
// */
//fun <ID> MongoRepository<AvocadoReceipt, ID>.saveAll(entities: Iterable<AvocadoReceipt>): List<AvocadoReceipt> =
//        saveAll(entities.map { it.copy() })
