package io.holyguacamole.bot.message

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Repository
class AvocadoReceiptRepository(
        private val mongoRepository: AvocadoReceiptMongoRepository,
        private val template: MongoTemplate) {

    fun findByEventId(eventId: String): List<AvocadoReceipt> = mongoRepository.findByEventId(eventId)

    fun findAll(): List<AvocadoReceipt> = mongoRepository.findAll()

    fun deleteAll() = mongoRepository.deleteAll()

    fun saveAll(entities: Iterable<AvocadoReceipt>): List<AvocadoReceipt> =
            mongoRepository.saveAll(entities.map { it.copy() })

    fun getLeaderboard(limit: Long = 10): List<AvocadoCount> =
            template.aggregate(
                    Aggregation.newAggregation(
                            Aggregation.group("receiver")
                                    .count().`as`("count"), //TODO do we actually need the as?
                            Aggregation.sort(Sort.Direction.DESC, "count"),
                            Aggregation.project("receiver", "count"),
                            Aggregation.limit(limit)
                    ),
                    AvocadoReceipt::class.java,
                    AvocadoCount::class.java
            ).toList()

    fun findBySenderToday(sender: String): List<AvocadoReceipt> = mongoRepository.findBySenderAndTimestampGreaterThan(sender, LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).toEpochSecond(ZoneOffset.UTC))

    fun deleteBySenderAndTimestamp(sender: String, timestamp: Long) {
        mongoRepository.deleteBySenderAndTimestamp(sender, timestamp)
    }
}

@Repository
interface AvocadoReceiptMongoRepository : MongoRepository<AvocadoReceipt, String> {
    fun findByEventId(eventId: String): List<AvocadoReceipt>
    fun findBySenderAndTimestampGreaterThan(sender: String, timestamp: Long): List<AvocadoReceipt>
    fun deleteBySenderAndTimestamp(sender: String, timestamp: Long)
}

data class AvocadoReceipt(
        val id: String? = null,
        val eventId: String,
        val sender: String,
        val receiver: String,
        val timestamp: Long,
        val message: String
)

data class AvocadoCount(
        @Id
        val receiver: String,
        val count: Int
)
