package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
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
    fun `it receives a message event and writes an AvocadoReceipt to the database`() {

        val response = controller.message(MockMessages.withSingleMentionAndAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = repository.findAll()
        assert(records.first().id).isNotNull()
        assert(records.first().sender).isEqualTo("U12356")
        assert(records.first().receiver).isEqualTo("U0LAN0Z89")
        assert(records.first().eventId).isEqualTo("12345678")
    }

    @Test
    fun `it does not write an AvocadoReceipt if there is no mention and avocado`() {

        val response = controller.message(MockMessages.withoutMentionAndAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = repository.findAll()
        assert(records).isEmpty()
    }
}
