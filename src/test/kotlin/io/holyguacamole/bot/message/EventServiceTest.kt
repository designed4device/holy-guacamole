package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.nhaarman.mockito_kotlin.*
import io.holyguacamole.bot.Empty
import io.holyguacamole.bot.MockAppMentions
import io.holyguacamole.bot.MockAvocadoReceipts
import io.holyguacamole.bot.MockChannels.directMessage
import io.holyguacamole.bot.MockChannels.general
import io.holyguacamole.bot.MockDirectMessages
import io.holyguacamole.bot.MockIds.appbot
import io.holyguacamole.bot.MockIds.dwayne
import io.holyguacamole.bot.MockIds.eight
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.jeremyp
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.MockIds.ryan
import io.holyguacamole.bot.MockJoinedChannelEvents
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.MockTeamJoinEvents
import io.holyguacamole.bot.MockUserChangeEvent.markNameUpdate
import io.holyguacamole.bot.MockUsers.dwaynetheguacjohnson
import io.holyguacamole.bot.MockUsers.eightrib
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.holyguacamole
import io.holyguacamole.bot.MockUsers.jeremypiewalker
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.MockUsers.ryanjwellen
import io.holyguacamole.bot.controller.MessageEvent
import io.holyguacamole.bot.message.ContentCrafter.welcomeMessage
import io.holyguacamole.bot.message.EventService.Companion.countGuacamoleIngredients
import io.holyguacamole.bot.message.EventService.Companion.findMentionedPeople
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
        whenever(it.findByUserIdOrGetFromSlack(ryanjwellen.userId)) doReturn ryanjwellen
        whenever(it.findByUserIdOrGetFromSlack(eightrib.userId)) doReturn eightrib
        whenever(it.findByUserIdOrGetFromSlack(jeremypiewalker.userId)) doReturn jeremypiewalker
        whenever(it.findByUserIdOrGetFromSlack(dwaynetheguacjohnson.userId)) doReturn dwaynetheguacjohnson
    }

    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.saveAll(anyList<AvocadoReceipt>())) doReturn emptyList<AvocadoReceipt>()
        whenever(it.findByEventId(any())) doReturn emptyList<AvocadoReceipt>()
    }
    private val eventService = EventService(repository, slackClient, userService, appbot, 10, "CHANNELID")

    @Test
    fun `it knows how many avocados someone is trying to send`() {
        assert(Empty.messageEvent.copy(text = ":avocado:").let { countGuacamoleIngredients(it.text!!) }).isEqualTo(1)
        assert(Empty.messageEvent.copy(text = ":avocado: :avocado:").let { countGuacamoleIngredients(it.text!!) }).isEqualTo(2)
        assert(Empty.messageEvent.copy(text = ":avocado::avocado:").let { countGuacamoleIngredients(it.text!!) }).isEqualTo(2)
        assert(Empty.messageEvent.copy(text = ":avocado:avocado:").let { countGuacamoleIngredients(it.text!!) }).isEqualTo(1)
    }

    @Test
    fun `it knows who the avocados were given to`() {
        val event = Empty.messageEvent.copy(text = "<@USER1> <@USER2>")

        assert(findMentionedPeople(event.text!!, event.user!!)).containsAll("USER1", "USER2")
    }

    @Test
    fun `it knows if someone is not trying to send an avocado`() {
        val event = MockMessages.withoutMentionAndAvocado.event as MessageEvent

        assert(countGuacamoleIngredients(event.text!!)).isEqualTo(0)
        assert(findMentionedPeople(event.text!!, event.user!!)).isEmpty()
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

        verify(slackClient).sendDirectMessage(user = eq(mark), text = any(), attachments = any())
        verify(slackClient).sendDirectMessage(user = eq(patrick), text = any(), attachments = any())
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

        val leaderboard = """
            1. ${jeremyskywalker.name}: 3
            2. ${feeneyfeeneybobeeney.name}: 2
            3. ${markardito.name}: 1
            """.trimIndent()

        verify(slackClient).postMessage(general, leaderboard)
    }

    @Test
    fun `it checks for the leaderboard count and posts the correct leaderboard to slack`() {

        whenever(repository.getLeaderboard(1)).thenReturn(listOf(
            AvocadoCount(jeremy, 3)
        ))
        whenever(repository.getLeaderboard(12)).thenReturn(listOf(
            AvocadoCount(jeremy, 12),
            AvocadoCount(mark, 5),
            AvocadoCount(patrick, 3)
        ))

        eventService.process(MockAppMentions.leaderboard1)
        verify(slackClient).postMessage(general, "1. ${jeremyskywalker.name}: 3")

        eventService.process(MockAppMentions.leaderboard12)
        verify(slackClient).postMessage(general,
            """
                  1. ${jeremyskywalker.name}: 12
                  2. ${markardito.name}: 5
                  3. ${feeneyfeeneybobeeney.name}: 3
                """.trimIndent()
        )
    }

    @Test
    fun `it checks for the 'leaderboard me' and posts the leaderboard +- 2 from the user to slack`() {
        whenever(repository.getLeaderboard(0)).thenReturn(listOf(
            AvocadoCount(ryan, 12),
            AvocadoCount(jeremy, 12),
            AvocadoCount(dwayne, 8),
            AvocadoCount(mark, 5),
            AvocadoCount(jeremyp, 4),
            AvocadoCount(patrick, 3),
            AvocadoCount(eight, 1)
        ))

        eventService.process(MockAppMentions.leaderboardMe)
        verify(slackClient).postMessage(general,
            """
                  2. ${jeremyskywalker.name}: 12
                  3. ${dwaynetheguacjohnson.name}: 8
                  4. ${markardito.name}: 5
                  5. ${jeremypiewalker.name}: 4
                  6. ${feeneyfeeneybobeeney.name}: 3
                """.trimIndent()
        )
    }

    @Test
    fun `it checks for the 'leaderboard otherUser' and posts the leaderboard +- 2 from the user to slack`() {
        whenever(repository.getLeaderboard(0)).thenReturn(listOf(
                AvocadoCount(ryan, 12),
                AvocadoCount(jeremy, 12),
                AvocadoCount(dwayne, 8),
                AvocadoCount(mark, 5),
                AvocadoCount(jeremyp, 4),
                AvocadoCount(patrick, 3),
                AvocadoCount(eight, 1)
        ))

        eventService.process(MockAppMentions.leaderboardOtherUser)
        verify(slackClient).postMessage(general,
                """
                  2. ${jeremyskywalker.name}: 12
                  3. ${dwaynetheguacjohnson.name}: 8
                  4. ${markardito.name}: 5
                  5. ${jeremypiewalker.name}: 4
                  6. ${feeneyfeeneybobeeney.name}: 3
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

        verify(userService).replace(markardito.copy(name = "jebardito"))
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

        verify(repository).revokeAvocadosBySenderAndTimestamp(jeremy, (deleteMessage.event as MessageEvent).previousMessage?.ts?.toTimestamp()!!)
        verifyNoMoreInteractions(repository)
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
    fun `it sends an ephemeral message to user when they delete avocados`() {
        val deleteMessage = MockMessages.withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosToday
        val deleteMessageEvent = (deleteMessage.event as MessageEvent)

        whenever(repository.revokeAvocadosBySenderAndTimestamp(any(), any())).thenReturn(listOf(
            AvocadoCount(mark, 2), AvocadoCount(patrick, 2)
        ))

        eventService.process(deleteMessage)

        verify(slackClient).postEphemeralMessage(
            channel = eq(deleteMessageEvent.channel),
            user = eq(deleteMessageEvent.previousMessage?.user!!),
            text = any()
        )
    }

    @Test
    fun `it sends the message with guacward message and help command message if command passed is invalid`() {
        eventService.process(MockAppMentions.unknownCommand)
        verify(slackClient).postMessage(eq(general), eq(""), check {
            assert(it.size).isEqualTo(1)
            it.first().let {
                assert(it.title).isEmpty()
                assert(it.text).isNotEmpty()
                assert(it.pretext).isNotEmpty()
                assert(it.markdownIn).isEqualTo(listOf(MARKDOWN.TEXT))
            }
        })
    }

    @Test
    fun `it sends direct message to previous avocado receiver about revoked avocado`() {
        val deleteMessage = MockMessages.withDeleteSubTypeForMultipleMentionsAndMultipleAvocadosToday

        whenever(repository.revokeAvocadosBySenderAndTimestamp(any(), any())).thenReturn(listOf(
            AvocadoCount(mark, 2), AvocadoCount(patrick, 2)
        ))

        eventService.process(deleteMessage)

        verify(slackClient).postEphemeralMessage(any(), any(), any())

        verify(slackClient).sendDirectMessage(eq(mark), any(), check {
            assert(it.size).isEqualTo(1)
            it.first().let {
                assert(it.title).isEmpty()
                assert(it.pretext).isNotEmpty()
                assert(it.text).isNotEmpty()
                assert(it.markdownIn).isEqualTo(listOf(MARKDOWN.TEXT, MARKDOWN.PRETEXT))
            }
        })
        verify(slackClient).sendDirectMessage(eq(patrick), any(), any())
    }

    @Test
    fun `it sends the message text in the avocado received message`() {
        eventService.process(MockMessages.withSingleMentionAndSingleAvocado)

        verify(slackClient).sendDirectMessage(user = eq(mark), text = any(), attachments = eq(listOf(MessageAttachment(
            title = "",
            pretext = "",
            text = (MockMessages.withSingleMentionAndSingleAvocado.event as MessageEvent).text!!,
            markdownIn = listOf(MARKDOWN.TEXT)
        ))))
    }

    @Test
    fun `it calls the slack client to direct message when a user joins the team`() {
        eventService.process(MockTeamJoinEvents.jeremyJoinedTeam)

        verify(slackClient).sendDirectMessage(user = jeremy, attachments = listOf(welcomeMessage))
    }

    @Test
    fun `it does not call the slack client to send a direct message when a new bot joins the team`() {
        eventService.process(MockTeamJoinEvents.botJoinedTeam)

        verifyZeroInteractions(slackClient)
    }

    @Test
    fun `it doesn't send a milecado message when the milecado was previously sent`() {
        whenever(repository.count()).thenReturn(10)

        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(repository).count()
        verify(slackClient, times(0)).postMessage(any(), any(), any())
    }

    @Test
    fun `it doesn't send a milecado message when the milecado is not sent by current message`() {
        whenever(repository.count()).thenReturn(1)

        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(repository).count()
        verify(slackClient, times(0)).postMessage(any(), any(), any())
    }

    @Test
    fun `it sends a message when the milecado is sent by current message`() {
        whenever(repository.count()).thenReturn(9)

        eventService.process(MockMessages.withSingleMentionAndMultipleAvocados)

        verify(repository).count()
        verify(slackClient).postMessage(
                channel = eq("CHANNELID"),
                text = eq(""),
                attachments = eq(listOf(MessageAttachment(
                        pretext = ":flashing_alarm: The 10th avocado has been given!!! :flashing_alarm:",
                        text = (MockMessages.withSingleMentionAndMultipleAvocados.event as MessageEvent).text!!,
                        title = "",
                        markdownIn = listOf(MARKDOWN.TEXT)
                )))
        )
    }
}

class DirectMessageEventTests {

    private val slackClient: SlackClient = mock()
    private val userService: UserService = mock {
        whenever(it.findByUserIdOrGetFromSlack(markardito.userId)) doReturn markardito
        whenever(it.findByUserIdOrGetFromSlack(holyguacamole.userId)) doReturn holyguacamole
        whenever(it.findByUserIdOrGetFromSlack(feeneyfeeneybobeeney.userId)) doReturn feeneyfeeneybobeeney
        whenever(it.findByUserIdOrGetFromSlack(jeremyskywalker.userId)) doReturn jeremyskywalker
        whenever(it.findByUserIdOrGetFromSlack(ryanjwellen.userId)) doReturn ryanjwellen
        whenever(it.findByUserIdOrGetFromSlack(eightrib.userId)) doReturn eightrib
        whenever(it.findByUserIdOrGetFromSlack(jeremypiewalker.userId)) doReturn jeremypiewalker
        whenever(it.findByUserIdOrGetFromSlack(dwaynetheguacjohnson.userId)) doReturn dwaynetheguacjohnson
    }
    private val repository: AvocadoReceiptRepository = mock {
        whenever(it.findBySenderToday(patrick)) doReturn emptyList<AvocadoReceipt>()
    }
    private val eventService = EventService(repository, slackClient, userService, appbot, 10, "CHANNELID")

    @Test
    fun `it sends a dm with the number of avocados left to send`() {
        eventService.process(MockDirectMessages.avocados)

        verify(repository).findBySenderToday(patrick)
        verify(slackClient).postMessage(eq(directMessage), eq(ContentCrafter.avocadosLeft(5)), any())
    }

    @Test
    fun `it sends a dm with the help command text`() {
        eventService.process(MockDirectMessages.withHelp)
        val channel = (MockDirectMessages.withHelp.event as MessageEvent).channel

        verify(slackClient).postMessage(channel = eq(channel), text = eq(""), attachments = eq(ContentCrafter.helpMessage))
    }

    @Test
    fun `it does not send number of avocados if you don't send the avocados command`() {
        eventService.process(MockDirectMessages.withNoCommand)

        verifyZeroInteractions(repository)
        verifyZeroInteractions(slackClient)
    }

    @Test
    fun `it sends a dm with the leaderboard`() {
        eventService.process(MockDirectMessages.leaderboard)

        verify(slackClient).postMessage(eq(directMessage), any(), any())
    }

    @Test
    fun `it sends a dm with the leaderboard me`() {
        whenever(repository.getLeaderboard(0)).thenReturn(listOf(
            AvocadoCount(ryan, 12),
            AvocadoCount(jeremy, 12),
            AvocadoCount(dwayne, 8),
            AvocadoCount(mark, 5),
            AvocadoCount(jeremyp, 4),
            AvocadoCount(patrick, 3),
            AvocadoCount(eight, 1)
        ))

        eventService.process(MockDirectMessages.leaderboardMe)

        verify(slackClient).postMessage(
            eq(directMessage),
            eq("""
                  2. ${jeremyskywalker.name}: 12
                  3. ${dwaynetheguacjohnson.name}: 8
                  4. ${markardito.name}: 5
                  5. ${jeremypiewalker.name}: 4
                  6. ${feeneyfeeneybobeeney.name}: 3
                """.trimIndent()),
            any())
    }

    @Test
    fun `it sends a dm with the leaderboard me when the user is last`() {
        whenever(repository.getLeaderboard(0)).thenReturn(listOf(
            AvocadoCount(ryan, 12),
            AvocadoCount(jeremy, 12),
            AvocadoCount(dwayne, 8),
            AvocadoCount(mark, 5)
        ))

        eventService.process(MockDirectMessages.leaderboardMe)

        verify(slackClient).postMessage(
            eq(directMessage),
            eq("""
                  2. ${jeremyskywalker.name}: 12
                  3. ${dwaynetheguacjohnson.name}: 8
                  4. ${markardito.name}: 5
                """.trimIndent()),
            any())
    }

    @Test
    fun `it sends a dm with the leaderboard me when the user is first`() {
        whenever(repository.getLeaderboard(0)).thenReturn(listOf(
            AvocadoCount(mark, 5),
            AvocadoCount(jeremyp, 4),
            AvocadoCount(patrick, 3),
            AvocadoCount(eight, 1)
        ))

        eventService.process(MockDirectMessages.leaderboardMe)

        verify(slackClient).postMessage(
            eq(directMessage),
            eq("""
                  1. ${markardito.name}: 5
                  2. ${jeremypiewalker.name}: 4
                  3. ${feeneyfeeneybobeeney.name}: 3
                """.trimIndent()),
            any())
    }
}
