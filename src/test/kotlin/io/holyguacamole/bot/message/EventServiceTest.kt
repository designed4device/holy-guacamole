package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.Empty
import io.holyguacamole.bot.MockAppMentions
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockChannels.general
import io.holyguacamole.bot.MockIds.appbot
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.MockJoinedChannelEvents
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.MockUserChangeEvent.markNameUpdate
import io.holyguacamole.bot.MockUsers.eightRib
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.holyguacamole
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.message.ContentCrafter.welcomeMessage
import io.holyguacamole.bot.user.UserService
import org.junit.Test
import org.mockito.Mockito.anyList

class EventServiceTest {

    private val slackClient: SlackClient = mock()
    private val userService: UserService = mock {
        whenever(it.findByUserIdOrGetFromSlack(markardito.userId)) doReturn markardito
        whenever(it.findByUserIdOrGetFromSlack(holyguacamole.userId)) doReturn holyguacamole
        whenever(it.findByUserIdOrGetFromSlack(feeneyfeeneybobeeney.userId)) doReturn feeneyfeeneybobeeney
        whenever(it.findByUserIdOrGetFromSlack(jeremyskywalker.userId)) doReturn jeremyskywalker
    }

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(anyList<AvocadoReceipt>())) doReturn emptyList<AvocadoReceipt>()
        whenever(it.findByEventId(any())) doReturn emptyList<AvocadoReceipt>()
    }
    private val eventService = EventService(repository, slackClient, userService, appbot)

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        assert(Empty.messageEvent.copy(text = ":avocado:").countGuacamoleIngredients()).isEqualTo(1)
        assert(Empty.messageEvent.copy(text = ":avocado: :avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(Empty.messageEvent.copy(text = ":avocado::avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(Empty.messageEvent.copy(text = ":avocado:avocado:").countGuacamoleIngredients()).isEqualTo(1)
    }

    @Test
    fun `it knows who the avocados were given to`() {
        val event = Empty.messageEvent.copy(text = "<@USER1> <@USER2>")

        assert(event.findMentionedPeople()).containsAll("USER1", "USER2")
    }

    @Test
    fun `it knows if someone is not trying to send an avocado`() {
        val event = MockMessages.withoutMentionAndAvocado.event as MessageEvent

        assert(event.countGuacamoleIngredients()).isEqualTo(0)
        assert(event.findMentionedPeople()).isEmpty()
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
    fun `it does not add any AvocadoReceipts if the avocado is from a bot`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocadoFromBot)

        verifyZeroInteractions(repository)
    }

    @Test
    fun `it does not add AvocadoReceipts for bots`() {
        eventService.process(MockMessages.withBotMentionAndSingleAvocado)

        verify(repository).findByEventId(MockMessages.withBotMentionAndSingleAvocado.eventId)
        verify(repository).findBySenderToday((MockMessages.withBotMentionAndSingleAvocado.event as MessageEvent).user!!)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `it posts a message to the user after they send an avocado`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(slackClient).postEphemeralMessage(
                channel = eq(general),
                user = eq(patrick),
                text = any()
        )
    }

    @Test
    fun `it doesn't post a message to the user if user only sent avocados to a bot`() {
        eventService.process(MockMessages.withBotMentionAndSingleAvocado)

        verifyZeroInteractions(slackClient)
    }

    @Test
    fun `it sends a message to the user if they don't have enough avocados to give for multiple mentions`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.jeremyToMark,
                MockAvocadoReceipts.jeremyToMark
        ))
        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(slackClient).postEphemeralMessage(eq(general), eq(jeremy), any())
    }

    @Test
    fun `it sends a message to the user if they don't have enough avocados to give for single mention`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark
        ))
        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(slackClient).postEphemeralMessage(eq(general), eq(patrick), any())
    }

    @Test
    fun `it sends a direct message to the avocado receivers`() {
        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(slackClient).sendDirectMessage(user = eq(mark), text = any(), attachment = any())
        verify(slackClient).sendDirectMessage(user = eq(patrick), text = any(), attachment = any())
    }

    @Test
    fun `it does not reprocess the same message`() {
        whenever(repository.findByEventId(any()))
                .thenReturn(emptyList())
                .thenReturn(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)

        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(repository).saveAll(MockAvocadoReceipts.singleMentionAndSingleAvocadoReceipts)
    }

    @Test
    fun `it does not reprocess the same message even when the messages has multiple avocados`() {
        whenever(repository.findByEventId(any()))
                .thenReturn(emptyList())
                .thenReturn(MockAvocadoReceipts.multipleMentionsAndSingleAvocadoReceipts)

        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)
        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)

        verify(repository).saveAll(MockAvocadoReceipts.multipleMentionsAndMultipleAvocadosReceipts)
    }

    @Test
    fun `it calls the slack client to post the leaderboard`() {
        whenever(repository.getLeaderboard(10)).thenReturn(listOf(
                AvocadoCount(jeremy, 3),
                AvocadoCount(patrick, 2),
                AvocadoCount(mark, 1)
        ))

        eventService.process(MockAppMentions.leaderboard)

        val leaderboard = "${jeremyskywalker.name}: 3\n${feeneyfeeneybobeeney.name}: 2\n${markardito.name}: 1"

        verify(slackClient).postMessage(general, leaderboard)
    }

    @Test
    fun `it checks for leaderboard count and post the correct leaderboard to slack`() {

        whenever(repository.getLeaderboard(1)).thenReturn(listOf(
                AvocadoCount(jeremy, 3)
        ))
        whenever(repository.getLeaderboard(12)).thenReturn(listOf(
                AvocadoCount(jeremy, 12),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(mark, 5),
                AvocadoCount(patrick, 3),
                AvocadoCount(patrick, 3),
                AvocadoCount(patrick, 3),
                AvocadoCount(patrick, 3)
        ))

        eventService.process(MockAppMentions.leaderboard1)
        verify(slackClient).postMessage(general, "${jeremyskywalker.name}: 3")

        eventService.process(MockAppMentions.leaderboard12)
        verify(slackClient).postMessage(general,
                """
                  ${jeremyskywalker.name}: 12
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${markardito.name}: 5
                  ${feeneyfeeneybobeeney.name}: 3
                  ${feeneyfeeneybobeeney.name}: 3
                  ${feeneyfeeneybobeeney.name}: 3
                  ${feeneyfeeneybobeeney.name}: 3
                """.trimIndent()
        )
    }

    @Test
    fun `it calls the slack client to post the help message`() {
        eventService.process(MockAppMentions.help)

        verify(slackClient).postMessage(eq(general), eq(""), any())
    }

    @Test
    fun `it replaces a user`() {
        eventService.process(markNameUpdate)

        verify(userService).replace(eightRib)
    }

    @Test
    fun `it does not add AvocadoReceipts with single mention multiple avocados if the sender has already sent 5 today`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick
        ))
        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(repository).findBySenderToday(any())
        verifyZeroInteractions(repository)
    }

    @Test
    fun `it does not add AvocadoReceipts with multiple mentions single avocado if the sender has already sent 5 today`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick,
                MockAvocadoReceipts.markToPatrick
        ))
        eventService.process(MockMessages.withMultipleMentionsAndSingleAvocado)

        verify(repository).findBySenderToday(any())
        verifyZeroInteractions(repository)
    }

    @Test
    fun `it reminds users to use avocados if they send a mention and a taco`() {
        whenever(repository.findBySenderToday(any())).thenReturn(emptyList())

        eventService.process(MockMessages.withSingleMentionAndSingleTaco)
        eventService.process(MockMessages.withNoMentionAndSingleTaco)
        verify(slackClient).postEphemeralMessage(eq(general), eq(patrick), any())
    }

    @Test
    fun `it does not remind users to use avocados if they send a mention, a taco, and an avocado`() {
        whenever(repository.findBySenderToday(any())).thenReturn(emptyList())

        eventService.process(MockMessages.withSingleMentionSingleAvocadoAndSingleTaco)
        verify(slackClient).postEphemeralMessage(eq(general), eq(patrick), any())
    }

    @Test
    fun `it calls the slack client to post a message when the bot was invited to a channel`() {
        eventService.process(MockJoinedChannelEvents.botJoined)

        verify(slackClient).postMessage(channel = general, attachments = listOf(welcomeMessage))
    }

    @Test
    fun `it doesn't call the slack client if a regular user was invited to a channel`() {
        eventService.process(MockJoinedChannelEvents.markJoined)

        verifyZeroInteractions(slackClient)
    }

    @Test
    fun `it deletes the correct avocado receipts when a delete event is received`() {
        val deleteMessage = MockMessages.withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosToday
        eventService.process(deleteMessage)

        verify(repository).deleteBySenderAndTimestamp(jeremy, (deleteMessage.event as MessageEvent).previousMessage?.ts?.toTimestamp()!!)
        verifyNoMoreInteractions(repository)
        verifyNoMoreInteractions(slackClient)
        verifyNoMoreInteractions(userService)
    }

    @Test
    fun `it only deletes avocados if the message deleted was posted the same day`() {
        val deleteMessage = MockMessages.withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosYesterday
        eventService.process(deleteMessage)

        verifyZeroInteractions(repository)
        verifyZeroInteractions(slackClient)
        verifyZeroInteractions(userService)
    }

    @Test
    fun `it sends the message text in the avocado received message`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(slackClient).sendDirectMessage(user = eq(mark), text = any(), attachment = eq("<@$mark> you're the best ${ContentCrafter.AVOCADO_TEXT}"))
    }
}
