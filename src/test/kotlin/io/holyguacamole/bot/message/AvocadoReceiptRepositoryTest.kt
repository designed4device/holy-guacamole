package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockIds
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(SpringRunner::class)
@DataMongoTest
class AvocadoReceiptRepositoryTest {

    @Autowired private lateinit var mongoTemplate: MongoTemplate
    @Autowired private lateinit var mongoRepository: AvocadoReceiptMongoRepository

    private lateinit var repository: AvocadoReceiptRepository

    @Before
    fun setUp() {
        repository = AvocadoReceiptRepository(mongoRepository, mongoTemplate)
    }

    @After
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `it finds AvocadoReceipts by eventId`() {
        repository.saveAll(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)

        val avocadoReceipt = MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.first()

        assert(repository.findByEventId(avocadoReceipt.eventId).nullifyIds()).containsExactly(avocadoReceipt)
    }

    @Test
    fun `it retrieves a count of receipts grouped by receiver and sorted by count in descending order`() {
        repository.saveAll(listOf(
                MockAvocadoReceipts.markToJeremy,
                MockAvocadoReceipts.markToJeremy,
                MockAvocadoReceipts.patrickToJeremy,
                MockAvocadoReceipts.jeremyToPatrick,
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.patrickToMark
        ))

        assert(repository.getLeaderboard()).containsExactly(
                AvocadoCount(jeremy, 3),
                AvocadoCount(patrick, 2),
                AvocadoCount(mark, 1)
        )
    }

    @Test
    fun `it retrieves all avocados a user sent today`() {
        repository.saveAll(listOf(
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        ))

        assert(repository.findBySenderToday(MockIds.patrick)).hasSize(5)
    }
}