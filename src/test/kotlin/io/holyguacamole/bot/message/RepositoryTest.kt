package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsExactly
import io.holyguacamole.bot.MockAvocadoReceipts
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
        repository.saveAvocadoReceipts(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)
    }

    @After
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `it finds AvocadoReceipts by eventId`() {
        val avocadoReceipt = MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.first()

        assert(repository.findByEventId(avocadoReceipt.eventId).nullifyIds()).containsExactly(avocadoReceipt)
    }
}