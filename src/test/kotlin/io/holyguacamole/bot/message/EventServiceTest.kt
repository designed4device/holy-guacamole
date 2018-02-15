package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.MockAppMentions
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.controller.Event
import io.holyguacamole.bot.controller.EventCallback
import org.junit.Test
import org.mockito.Mockito.anyList

class EventServiceTest {

    private val chatService: ChatService = mock()

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(anyList<AvocadoReceipt>())) doReturn emptyList<AvocadoReceipt>()
        whenever(it.findByEventId(any())) doReturn emptyList<AvocadoReceipt>()
    }
    private val eventService = EventService(repository, chatService)

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        assert(emptyEvent.copy(text = ":avocado:").countGuacamoleIngredients()).isEqualTo(1)
        assert(emptyEvent.copy(text = ":avocado: :avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(emptyEvent.copy(text = ":avocado::avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(emptyEvent.copy(text = ":avocado:avocado:").countGuacamoleIngredients()).isEqualTo(1)
    }

    @Test
    fun `it knows who the avocados were given to`() {
        val event = emptyEvent.copy(text = "<@USER1> <@USER2>")

        assert(event.findMentionedPeople()).containsAll("USER1", "USER2")
    }

    @Test
    fun `it knows if someone is not trying to send an avocado`() {
        val eventCallback = MockMessages.withoutMentionAndAvocado

        assert(eventCallback.event.countGuacamoleIngredients()).isEqualTo(0)
        assert(eventCallback.event.findMentionedPeople()).isEmpty()
    }

    @Test
    fun `it does not create an AvocadoReceipt if it's just a normal message`() {
        eventService.process(MockMessages.withoutMentionAndAvocado)

        verifyZeroInteractions(repository)
    }

    @Test
    fun `it creates AvocadoReceipts when someone is trying to send an avocado`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        val expectedRecords = MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts
        verify(repository).saveAll(expectedRecords)
    }

    @Test
    fun `it creates an AvocadoReceipt for each avocado in the message`() {
        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        val expectedRecords = MockAvocadoReceipts.singleMentionAndMultipleAvocadosReceipts
        verify(repository).saveAll(expectedRecords)
    }

    @Test
    fun `it creates an AvocadoReceipt for each user in the message with a single avocado`() {
        eventService.process(MockMessages.withMultipleMentionsAndSingleAvocado)

        verify(repository).saveAll(MockAvocadoReceipts.multipleMentionsAndSingleAvocadoReceipts)
    }

    @Test
    fun `it creates an AvocadoReceipt for each user and each avocado in the message`() {
        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(repository).saveAll(MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts)
    }

    @Test
    fun `it does not add any AvocadoReceipts if the user sends themself an avocado`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocadoFromThemself)

        verifyZeroInteractions(repository)
    }

    @Test
    fun `it does not reprocess the same message`() {
        whenever(repository.findByEventId(any()))
                .thenReturn(emptyList())
                .thenReturn(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)

        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(repository, times(1)).saveAll(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)
    }

    @Test
    fun `it does not reprocess the same message even when the messages has multiple avocados`() {
        whenever(repository.findByEventId(any()))
                .thenReturn(emptyList())
                .thenReturn(MockAvocadoReceipts.multipleMentionsAndSingleAvocadoReceipts)

        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)
        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(repository, times(1)).saveAll(MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts)
    }

    @Test
    fun `it returns true when it receives an app_mention event requesting the leaderboard`() {
        assert(eventService.process(MockAppMentions.leaderboard)).isTrue()
        assert(eventService.process(MockAppMentions.showMeTheLeaderboard)).isTrue()
    }

    @Test
    fun `it calls the chat service to post the leaderboard`() {
        val eventCallback = emptyEventCallback.copy(
                event = emptyEvent.copy(type = "app_mention", channel = "GENERAL", text = "leaderboard")
        )
        eventService.process(eventCallback)

        verify(chatService).postLeaderboard("GENERAL")
    }

    private val emptyEvent = Event(type = "", channel = "", user = "", text = "", ts = "")
    private val emptyEventCallback = EventCallback(
            token = "",
            type = "",
            teamId = "",
            apiAppId = "",
            event = emptyEvent,
            authedUsers = emptyList(),
            eventId = "",
            eventTime = 0L
    )
}