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
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.MockAppMentions
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockChannels
import io.holyguacamole.bot.MockChannels.general
import io.holyguacamole.bot.MockIds
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.MockJoinedChannelEvents
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.MockUserChangeEvent
import io.holyguacamole.bot.MockUsers
import io.holyguacamole.bot.controller.EventCallback
import io.holyguacamole.bot.controller.EventCallbackType.APP_MENTION
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.controller.UserChangeEvent
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import io.holyguacamole.bot.user.UserService
import org.junit.Test
import org.mockito.Mockito.anyList

class EventServiceTest {

    private val slackClient: SlackClient = mock()
    private val userService: UserService = mock {
        whenever(it.findByUserIdOrGetFromSlack(MockUsers.markardito.userId)) doReturn MockUsers.markardito
        whenever(it.findByUserIdOrGetFromSlack(MockUsers.holyguacamole.userId)) doReturn MockUsers.holyguacamole
        whenever(it.findByUserIdOrGetFromSlack(MockUsers.feeneyfeeneybobeeney.userId)) doReturn MockUsers.feeneyfeeneybobeeney
        whenever(it.findByUserIdOrGetFromSlack(MockUsers.jeremyskywalker.userId)) doReturn MockUsers.jeremyskywalker
    }

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(anyList<AvocadoReceipt>())) doReturn emptyList<AvocadoReceipt>()
        whenever(it.findByEventId(any())) doReturn emptyList<AvocadoReceipt>()
    }
    private val eventService = EventService(repository, slackClient, userService, MockIds.appbot)

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        assert(emptyMessageEvent.copy(text = ":avocado:").countGuacamoleIngredients()).isEqualTo(1)
        assert(emptyMessageEvent.copy(text = ":avocado: :avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(emptyMessageEvent.copy(text = ":avocado::avocado:").countGuacamoleIngredients()).isEqualTo(2)
        assert(emptyMessageEvent.copy(text = ":avocado:avocado:").countGuacamoleIngredients()).isEqualTo(1)
    }

    @Test
    fun `it knows who the avocados were given to`() {
        val event = emptyMessageEvent.copy(text = "<@USER1> <@USER2>")

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
        verify(repository).findBySenderToday((MockMessages.withBotMentionAndSingleAvocado.event as MessageEvent).user)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `it posts a message to the user after they send an avocado`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(slackClient).postSentAvocadoMessage(
                channel = MockChannels.general,
                sender = patrick,
                avocadosEach = 1,
                receivers = listOf(mark),
                remainingAvocados = 4
        )
    }

    @Test
    fun `it doesn't post a message to the user if user only sent avocados to a bot`() {
        eventService.process(MockMessages.withBotMentionAndSingleAvocado)

        verifyZeroInteractions(slackClient)
    }

    @Test
    fun `it sends a message to the user if they don't have enough avocados to give`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark,
                MockAvocadoReceipts.patrickToMark
        ))
        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(slackClient).postNotEnoughAvocadosMessage(general, patrick, 1)
    }

    @Test
    fun `it sends a direct message to the avocado receivers`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)
        verify(slackClient).sendAvocadoReceivedDirectMessage(user = mark, avocadosReceived = 1, sender = patrick)

        eventService.process(MockMessages.withMultipleMentionsAndMultipleAvocados)
        verify(slackClient).sendAvocadoReceivedDirectMessage(user = mark, avocadosReceived = 2, sender = jeremy)
        verify(slackClient).sendAvocadoReceivedDirectMessage(user = patrick, avocadosReceived = 2, sender = jeremy)
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
                event = emptyMessageEvent.copy(type = APP_MENTION, channel = "GENERAL", text = "leaderboard")
        )
        eventService.process(eventCallback)

        verify(slackClient).postLeaderboard("GENERAL", mapOf())
    }

    @Test
    fun `it replaces a user`(){
        eventService.process(MockUserChangeEvent.markNameUpdate)

        verify(userService).replace(MockUsers.eightRib)
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
    fun `it sends a receipt message to the user that sent avocados`() {
        whenever(repository.findBySenderToday(any())).thenReturn(listOf(
                MockAvocadoReceipts.patrickToJeremy,
                MockAvocadoReceipts.patrickToMark
        ))

        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(slackClient).postSentAvocadoMessage(
                channel = MockChannels.general,
                sender = patrick,
                avocadosEach = 2,
                receivers = listOf(mark),
                remainingAvocados = 1
        )
    }

    @Test
    fun `it calls the slack client to post a message when the bot was invited to a channel`() {
        eventService.process(MockJoinedChannelEvents.botJoined)

        verify(slackClient).postWelcomeMessage(channel = MockChannels.general)
    }

    @Test
    fun `it doesn't call the slack client if a regular user was invited to a channel`() {
        eventService.process(MockJoinedChannelEvents.markJoined)

        verifyZeroInteractions(slackClient)
    }

    private val emptyMessageEvent = MessageEvent(type = "", channel = "", user = "", text = "", ts = "")
    private val emptyUserChangeEvent = UserChangeEvent(
            type = "",
            user = SlackUser(
                    id = "",
                    name = "",
                    profile = SlackUserProfile("", ""),
                    isBot = false,
                    isRestricted = false,
                    isUltraRestricted = false
            )
    )
    private val emptyEventCallback = EventCallback(
            token = "",
            type = "",
            teamId = "",
            apiAppId = "",
            event = emptyMessageEvent,
            authedUsers = emptyList(),
            eventId = "",
            eventTime = 0L
    )
}