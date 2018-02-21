package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.MockUserChangeEvent
import io.holyguacamole.bot.MockUsers
import io.holyguacamole.bot.controller.EventController
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.user.User
import io.holyguacamole.bot.user.UserRepository
import io.holyguacamole.bot.user.UserService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class MessageIntegrationTest {
    private val token = "thisisagoodtoken"

    @Autowired
    lateinit var receiptRepository: AvocadoReceiptRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var slackClient: SlackClient

    @Autowired
    lateinit var userService: UserService

    private lateinit var controller: EventController

    @Before
    fun setUp() {
        controller = EventController(token, EventService(receiptRepository, slackClient, userService))
    }

    @After
    fun tearDown() {
        userRepository.deleteAll()
        receiptRepository.deleteAll()
    }

    @Test
    fun `it does not write an AvocadoReceipt if there is no mention and avocado`() {

        val response = controller.message(MockMessages.withoutMentionAndAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = receiptRepository.findAll()
        assert(records).isEmpty()
    }

    @Test
    fun `it receives a message event and writes an AvocadoReceipt to the database`() {

        userRepository.saveAll(listOf(MockUsers.markardito, MockUsers.feeneyfeeneybobeeney))

        val response = controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        assert(response.statusCode).isEqualTo(OK)

        val records = receiptRepository.findAll()

        assert(records.size).isEqualTo(1)

        records.nullifyIds().apply {
            assert(this).containsAll(*MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.toTypedArray())
        }
    }

    @Test
    fun `it receives a message event with multiple mentions and multiple avocados and writes multiple AvocadoReceipts to the database`() {

        userRepository.saveAll(listOf(MockUsers.markardito, MockUsers.feeneyfeeneybobeeney, MockUsers.jeremyskywalker))

        val response = controller.message(MockMessages.withMultipleMentionsAndMultipleAvocados)
        assert(response.statusCode).isEqualTo(OK)

        val records = receiptRepository.findAll()

        assert(records.size).isEqualTo(4)

        records.nullifyIds().apply {
            assert(this).containsAll(*MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts.toTypedArray())
        }
    }

    @Test
    fun `it does not store duplicate avocados when the same event is received more than once`() {

        userRepository.saveAll(listOf(MockUsers.markardito, MockUsers.feeneyfeeneybobeeney))

        controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        controller.message(MockMessages.withSingleMentionAndSingleAvocado)
        controller.message(MockMessages.withSingleMentionAndSingleAvocado)

        assert(receiptRepository.findByEventId(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts.first().eventId))
                .hasSize(1)
    }

    @Test
    fun `it receives a user update event and updates the user in the database`() {
        userRepository.save(MockUsers.markardito)

        controller.message(MockUserChangeEvent.markNameUpdate)

        assert(userRepository.findAll().nullifyUserIds()).containsExactly(MockUsers.eightRib)
    }

    @Test
    fun `it does not store avocado receipts if the sender is a bot`() {
        userRepository.saveAll(listOf(MockUsers.holyguacamole, MockUsers.markardito))

        controller.message(MockMessages.withSingleMentionAndSingleAvocadoFromBot)

        assert(receiptRepository.findAll()).isEmpty()
    }

    @Test
    fun `it does not store avocado receipts for bots`() {
        userRepository.saveAll(listOf(MockUsers.holyguacamole, MockUsers.markardito))

        controller.message(MockMessages.withBotMentionAndSingleAvocado)

        assert(receiptRepository.findAll()).isEmpty()
    }

    @Test
    fun `a user is only allowed to give 5 avocados per day`() {
        userRepository.saveAll(listOf(MockUsers.feeneyfeeneybobeeney, MockUsers.markardito))

        val mockAvocado = MockMessages.withSingleMentionAndSingleAvocado
        val mockEvent = MockMessages.withSingleMentionAndSingleAvocado.event as MessageEvent

        controller.message(mockAvocado.copy(eventId = "1", event = mockEvent.copy(ts = "${LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "2", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "3", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "4", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "5", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "6", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))
        controller.message(mockAvocado.copy(eventId = "7", event = mockEvent.copy(ts = "${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}")))

        assert(receiptRepository.findAll()).hasSize(6)
    }
}

fun List<AvocadoReceipt>.nullifyIds(): List<AvocadoReceipt> = this.map { it.copy(id = null) }
fun List<User>.nullifyUserIds(): List<User> = this.map { it.copy(id = null) }
