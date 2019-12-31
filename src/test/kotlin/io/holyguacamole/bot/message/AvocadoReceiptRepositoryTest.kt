package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockAvocadoReceipts.markToPatrick
import io.holyguacamole.bot.MockAvocadoReceipts.patrickToMark
import io.holyguacamole.bot.MockIds
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.helper.HGEpochSeconds
import io.holyguacamole.bot.helper.HGEpochSecondsNow
import io.holyguacamole.nullifyIds
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

@RunWith(SpringRunner::class)
@DataMongoTest
class AvocadoReceiptRepositoryTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate
    @Autowired
    private lateinit var mongoRepository: AvocadoReceiptMongoRepository

    private lateinit var repository: AvocadoReceiptRepository

    private val resetMonth = 1
    private val resetDay = 1

    @Before
    fun setUp() {
        repository = AvocadoReceiptRepository(mongoRepository, mongoTemplate, resetMonth, resetDay)
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

        assert(repository.getLeaderboard(10)).containsExactly(
            AvocadoCount(jeremy, 3),
            AvocadoCount(patrick, 2),
            AvocadoCount(mark, 1)
        )
    }

    @Test
    fun `it retrieves an unlimited count of receipts grouped by receiver and sorted by count in descending order and timestamp in ascending order`() {
        repository.saveAll(listOf(
            MockAvocadoReceipts.markToJeremy,
            MockAvocadoReceipts.markToJeremy,
            MockAvocadoReceipts.patrickToJeremy,
            MockAvocadoReceipts.jeremyToPatrick,
            MockAvocadoReceipts.markToPatrick,
            MockAvocadoReceipts.patrickToMark,
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 1, receiver = "NEWUSER1"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 2, receiver = "NEWUSER2"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 3, receiver = "NEWUSER3"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 4, receiver = "NEWUSER4"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 5, receiver = "NEWUSER5"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 6, receiver = "NEWUSER6"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 7, receiver = "NEWUSER7"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 8, receiver = "NEWUSER8"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 9, receiver = "NEWUSER9"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 10, receiver = "NEWUSER10"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 11, receiver = "NEWUSER11"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 12, receiver = "NEWUSER12"),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = patrickToMark.timestamp + 13, receiver = "NEWUSER13")
        ))

        assert(repository.getLeaderboard(0)).containsExactly(
            AvocadoCount(jeremy, 3),
            AvocadoCount(patrick, 2),
            AvocadoCount(mark, 1),
            AvocadoCount("NEWUSER1", 1),
            AvocadoCount("NEWUSER2", 1),
            AvocadoCount("NEWUSER3", 1),
            AvocadoCount("NEWUSER4", 1),
            AvocadoCount("NEWUSER5", 1),
            AvocadoCount("NEWUSER6", 1),
            AvocadoCount("NEWUSER7", 1),
            AvocadoCount("NEWUSER8", 1),
            AvocadoCount("NEWUSER9", 1),
            AvocadoCount("NEWUSER10", 1),
            AvocadoCount("NEWUSER11", 1),
            AvocadoCount("NEWUSER12", 1),
            AvocadoCount("NEWUSER13", 1)
        )
    }

    @Test
    fun `it breaks leaderboard ties by the person who received the tying avocado first`() {
        repository.saveAll(listOf(
            MockAvocadoReceipts.markToJeremy.copy(timestamp = HGEpochSecondsNow()),
            MockAvocadoReceipts.markToJeremy.copy(timestamp = HGEpochSecondsNow()),
            MockAvocadoReceipts.jeremyToPatrick.copy(timestamp = HGEpochSecondsNow() + 1),
            MockAvocadoReceipts.jeremyToPatrick.copy(timestamp = HGEpochSecondsNow() + 4),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSecondsNow() + 2),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSecondsNow() + 3)
        ))

        assert(repository.getLeaderboard(10)).containsExactly(
            AvocadoCount(jeremy, 2),
            AvocadoCount(mark, 2),
            AvocadoCount(patrick, 2)
        )
    }

    @Test
    fun `it retrieves all avocados a user sent today`() {
        repository.saveAll(listOf(
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.now().minusDays(2).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.of("America/Chicago")).minusMinutes(10).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.of("America/Chicago")).plusMinutes(1).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.MAX, ZoneId.of("America/Chicago")).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.NOON, ZoneId.of("America/Chicago")).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.MAX, ZoneId.of("America/Chicago")).minusMinutes(120).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.of("America/Chicago")).plusMinutes(120).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond())
        ))


        assert(repository.findBySenderToday(MockIds.patrick)).hasSize(5)
    }

    @Test
    fun `it deletes all avocado receipts for a user and timestamp`() {
        val timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        repository.saveAll(listOf(
            MockAvocadoReceipts.patrickToMark.copy(timestamp = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = timestamp),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = timestamp),
            MockAvocadoReceipts.patrickToJeremy.copy(timestamp = timestamp),
            MockAvocadoReceipts.patrickToJeremy.copy(timestamp = timestamp),
            MockAvocadoReceipts.jeremyToMark.copy(timestamp = timestamp)
        ))
        val deletedCount: List<AvocadoCount> = repository.revokeAvocadosBySenderAndTimestamp(patrick, timestamp)

        assert(repository.findAll()).hasSize(2)
        assert(deletedCount).isEqualTo(listOf(AvocadoCount(mark, 2), AvocadoCount(jeremy, 2)))
    }

    @Test
    fun `it limits the leaderboard`() {
        repository.saveAll(listOf(
            markToPatrick,
            markToPatrick,
            markToPatrick,
            markToPatrick.copy(receiver = "mark2"),
            markToPatrick.copy(receiver = "mark2"),
            markToPatrick.copy(receiver = "mark3"),
            markToPatrick.copy(receiver = "mark3"),
            markToPatrick.copy(receiver = "mark4"),
            markToPatrick.copy(receiver = "mark4"),
            markToPatrick.copy(receiver = "mark5"),
            markToPatrick.copy(receiver = "mark5"),
            markToPatrick.copy(receiver = "mark6"),
            markToPatrick.copy(receiver = "mark6"),
            markToPatrick.copy(receiver = "mark7"),
            markToPatrick.copy(receiver = "mark7"),
            markToPatrick.copy(receiver = "mark8"),
            markToPatrick.copy(receiver = "mark8"),
            markToPatrick.copy(receiver = "mark9"),
            markToPatrick.copy(receiver = "mark9"),
            markToPatrick.copy(receiver = "mark10"),
            markToPatrick.copy(receiver = "mark10"),
            markToPatrick.copy(receiver = "mark11")
        ))

        assert(repository.getLeaderboard()).hasSize(10)
        assert(repository.getLeaderboard(5)).hasSize(5)
        assert(repository.getLeaderboard(15)).hasSize(11)
    }

    @Test
    fun `it actually saves the message associated with the avocado`() {
        repository.saveAll(listOf(MockAvocadoReceipts.markToJeremy))

        assert(repository.findAll()[0].message).isEqualTo(MockAvocadoReceipts.markToJeremy.message)
    }

    @Test
    fun `it only returns avocados for the current season`() {
        repository.saveAll(listOf(
            //prior season avocados
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(1))),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.of(LocalDate.now().year, resetMonth, resetDay).minusDays(1), LocalTime.now())),
            //current season avocados
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now())),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now())),
            MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.of(LocalDate.now().year, resetMonth, resetDay), LocalTime.now()))
        ))

        assert(repository.getLeaderboard(10)).containsExactly(
            AvocadoCount(mark, 3)
        )
    }

    @Test
    fun `it returns the leaderboard by year`() {
        repository.saveAll(listOf(
                //two years ago avocados
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(2))),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(2))),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(2))),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(2))),
                //last year avocados
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(1))),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now().minusYears(1))),
                //current year avocados
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now())),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now())),
                MockAvocadoReceipts.patrickToMark.copy(timestamp = HGEpochSeconds(LocalDate.now()))
        ))

        val currentYear = LocalDate.now().year
        val lastYear = LocalDate.now().minusYears(1).year
        val twoYearsAgo = LocalDate.now().minusYears(2).year

        assert(repository.getLeaderboard(limit = 0, year = currentYear)).containsExactly(
                AvocadoCount(mark, 3)
        )

        assert(repository.getLeaderboard(limit = 0, year = lastYear)).containsExactly(
                AvocadoCount(mark, 2)
        )

        assert(repository.getLeaderboard(limit = 0, year = twoYearsAgo)).containsExactly(
                AvocadoCount(mark, 4)
        )
    }
}
