package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.controller.MessageEvent
import org.junit.Test
import org.mockito.Mockito.anyList

class MessageServiceTest {

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(anyList<AvocadoReceipt>())) doReturn emptyList<AvocadoReceipt>()
    }
    private val messageService = MessageService(repository)

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        val event = MessageEvent("", "", "", ":avocado:", "")

        assert(event.countGuacamoleIngredients()).isEqualTo(1)

        assert(event.copy(text = ":avocado: :avocado:").countGuacamoleIngredients()).isEqualTo(2)

        assert(event.copy(text = ":avocado::avocado:").countGuacamoleIngredients()).isEqualTo(2)

        assert(event.copy(text = ":avocado:avocado:").countGuacamoleIngredients()).isEqualTo(1)
    }

    @Test
    fun `it knows who the avocados were given to`() {
        val event = MessageEvent("", "", "", "<@USER1> <@USER2>", "")

        assert(event.findMentionedPeople()).containsAll("USER1", "USER2")
    }

    @Test
    fun `it knows if someone is not trying to send an avocado`() {
        val message = MockMessages.withoutMentionAndAvocado

        assert(message.event.countGuacamoleIngredients()).isEqualTo(0)
        assert(message.event.findMentionedPeople()).isEmpty()
    }

    @Test
    fun `it does not create an AvocadoReceipt if it's just a normal message`() {
        messageService.process(MockMessages.withoutMentionAndAvocado)

        verifyZeroInteractions(repository)
    }

    @Test
    fun `it creates AvocadoReceipts when someone is trying to send an avocado`() {
        messageService.process(MockMessages.withSingleMentionAndSingleAvocado)

        val expectedRecords = MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts
        verify(repository).saveAll(expectedRecords)
    }

    @Test
    fun `it creates an AvocadoReceipt for each avocado in the message`() {
        messageService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        val expectedRecords = MockAvocadoReceipts.singleMentionAndMultipleAvocadosReceipts
        verify(repository).saveAll(expectedRecords)
    }

    @Test
    fun `it creates an AvocadoReceipt for each user in the message with a single avocado`() {
        messageService.process(MockMessages.withMultipleMentionsAndSingleAvocado)

        verify(repository).saveAll(MockAvocadoReceipts.multipleMentionsAndSingleAvocadoReceipts)
    }

    @Test
    fun `it creates an AvocadoReceipt for each user and each avocado in the message`() {
        messageService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(repository).saveAll(MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts)
    }

}