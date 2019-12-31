package io.holyguacamole.bot.message

import io.holyguacamole.bot.helper.HGEpochSeconds
import io.holyguacamole.bot.helper.HGEpochSecondsNow
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class AvocadoReceiptRepository(
    private val mongoRepository: AvocadoReceiptMongoRepository,
    private val template: MongoTemplate,
    @Value("\${season.reset.month}") seasonResetMonth: Int,
    @Value("\${season.reset.day}") seasonResetDay: Int) {

    val resetEpoch = HGEpochSeconds(LocalDate.of(LocalDate.now().year, seasonResetMonth, seasonResetDay), LocalTime.MIDNIGHT)
        .let {
            if (it < (HGEpochSecondsNow())) it
            else HGEpochSeconds(LocalDate.of(LocalDate.now().year - 1, seasonResetMonth, seasonResetDay), LocalTime.MIDNIGHT)
        }

    fun findByEventId(eventId: String): List<AvocadoReceipt> = mongoRepository.findByEventId(eventId)

    fun findAll(): List<AvocadoReceipt> = mongoRepository.findAll()

    fun deleteAll() = mongoRepository.deleteAll()

    fun saveAll(entities: Iterable<AvocadoReceipt>): List<AvocadoReceipt> =
        mongoRepository.saveAll(entities.map { it.copy() })

    fun getLeaderboard(limit: Long = 10, year: Int = 0): List<AvocadoCount> =
        template.aggregate(
            Aggregation.newAggregation(
                listOf(Aggregation.match(
                        if (year == 0) Criteria("timestamp").gte(resetEpoch)
                        else Criteria("timestamp")
                                .gte(HGEpochSeconds(LocalDate.of(year, 1, 1), LocalTime.MIN))
                                .lte(HGEpochSeconds(LocalDate.of(year, 12, 31), LocalTime.MAX))
                ),
                Aggregation.group("receiver")
                    .max("timestamp").`as`("maxTimestamp")
                    .count().`as`("count"),
                Aggregation.sort(Sort.Direction.DESC, "count")
                    .and(Sort.Direction.ASC, "maxTimestamp"),
                Aggregation.project("receiver", "count"))
                    .let { if (limit == 0L) it else it.plus(Aggregation.limit(limit)) }
            ),
            AvocadoReceipt::class.java,
            AvocadoCount::class.java
        ).toList()

    fun findBySenderToday(sender: String): List<AvocadoReceipt> = mongoRepository.findBySenderAndTimestampGreaterThan(sender, ZonedDateTime.of(LocalDate.now(ZoneId.of("America/Chicago")), LocalTime.MIDNIGHT, ZoneId.of("America/Chicago")).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond())

    fun revokeAvocadosBySenderAndTimestamp(sender: String, timestamp: Long): List<AvocadoCount> {
        val receipts: List<AvocadoReceipt> = mongoRepository.findBySenderAndTimestamp(sender, timestamp)
        mongoRepository.deleteAll(receipts)

        return receipts.groupingBy { it.receiver }.eachCount().map { (receiver, count) ->
            AvocadoCount(receiver, count)
        }
    }
}

@Repository
interface AvocadoReceiptMongoRepository : MongoRepository<AvocadoReceipt, String> {
    fun findByEventId(eventId: String): List<AvocadoReceipt>
    fun findBySenderAndTimestampGreaterThan(sender: String, timestamp: Long): List<AvocadoReceipt>
    fun findBySenderAndTimestamp(sender: String, timestamp: Long): List<AvocadoReceipt>
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
