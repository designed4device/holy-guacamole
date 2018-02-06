package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockAvocadoReceipts.receipt
import io.holyguacamole.bot.MockMessages
import org.junit.Test

class MessageServiceTest {

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(listOf(any<AvocadoReceipt>()))) doReturn listOf(MockAvocadoReceipts.persistedReceipt)
    }
    private val messageService = MessageService(repository)

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        val message = MockMessages.withMultipleMentionsAndMultipleAvocados

        assert(message.event.countGuacamoleIngredients()).isEqualTo(2)
        assert(message.event.findMentionedPeople()).containsAll("U0LAN0Z89", "U0LAN0Z10")
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
        messageService.process(MockMessages.withSingleMentionAndAvocado)

        val expectedRecords = listOf(receipt)
        verify(repository).saveAll(expectedRecords)
    }

    @Test
    fun `it creates an AvocadoReceipt for each avocado in the message`() {
        messageService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        val expectedRecords = listOf(receipt, receipt)
        verify(repository).saveAll(expectedRecords)
    }

}