package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.controller.EventController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class MessageIntegrationTest {
    private val token = "thisisagoodtoken"

    @Autowired
    lateinit var repository: AvocadoReceiptRepository

    private lateinit var controller: EventController

    @Before
    fun setUp() {
        controller = EventController(token, MessageService(repository))
    }

    @After
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `it does not write an AvocadoReceipt if there is no mention and avocado`() {

        val response = controller.message(MockMessages.withoutMentionAndAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = repository.findAll()
        assert(records).isEmpty()
    }

    @Test
    fun `it receives a message event and writes an AvocadoReceipt to the database`() {

        val response = controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = repository.findAll()

        assert(records.size).isEqualTo(1)

        records.nullifyIds().apply {
            assert(this).containsAll(*MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.toTypedArray())
        }
    }

    @Test
    fun `it receives a message event with multiple mentions and multiple avocados and writes multiple AvocadoReceipts to the database`() {

        val response = controller.message(MockMessages.withMultipleMentionsAndMultipleAvocados)
        assert(response.statusCode).isEqualTo(OK)

        val records = repository.findAll()

        assert(records.size).isEqualTo(4)

        records.nullifyIds().apply {
            assert(this).containsAll(*MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts.toTypedArray())
        }
    }

    @Test
    fun `it does not store duplicate avocados when the same event is recieved more than once`() {
        controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        controller.message(MockMessages.withSingleMentionAndSingleAvocado)

        assert(repository.findByEventId(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.first().eventId))
                .hasSize(1)
    }


}

fun List<AvocadoReceipt>.nullifyIds(): List<AvocadoReceipt> = this.map { it.copy(id = null) } //TODO take care of this mike
