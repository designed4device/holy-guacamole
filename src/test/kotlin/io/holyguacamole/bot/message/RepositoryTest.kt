package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsExactly
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class RepositoryTest {

    @Autowired lateinit var repository: AvocadoReceiptRepository

    @Before
    fun setUp() {

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
}