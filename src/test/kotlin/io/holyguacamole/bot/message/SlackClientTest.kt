package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import io.holyguacamole.bot.slack.SlackUserResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick

class SlackClientTest {

    @Rule
    @JvmField
    var wireMockRule = WireMockRule(wireMockConfig().dynamicPort().dynamicPort())

    private lateinit var slackClient: SlackClient

    @Before
    fun setUp() {
        slackClient = SlackClient("http://localhost:${wireMockRule.port()}", "iamagoodbot")
    }

    @Test
    fun `it posts the leaderboard to the Slack API`() {
        stubFor(post(urlEqualTo("/api/chat.postMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postLeaderboard("GENERAL", mapOf(feeneyfeeneybobeeney.name to 3, jeremyskywalker.name to 2, markardito.name to 1))

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postMessage"))
                        .withRequestBody(equalTo("{\"channel\":\"$channel\"," +
                                "\"text\":\"${feeneyfeeneybobeeney.name}: 3\\n${jeremyskywalker.name}: 2\\n${markardito.name}: 1\"," +
                                "\"attachments\":[]" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it gets user info from the Slack API`() {
        stubFor(get(urlMatching("/api/users\\.info.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jacksonObjectMapper().writeValueAsString(userInfoResponse))
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.getUserInfo(feeneyfeeneybobeeney.userId)

        verify(
                getRequestedFor(urlMatching("/api/users\\.info.*"))
                        .withQueryParam("user", equalTo(feeneyfeeneybobeeney.userId))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it returns null when the user is not found`() {
        stubFor(get(urlMatching("/api/users\\.info.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jacksonObjectMapper().writeValueAsString(userNotFoundResponse))
                        .withHeader("Content-Type", "application/json")
                )
        )

        val response = slackClient.getUserInfo(feeneyfeeneybobeeney.userId)

        verify(
                getRequestedFor(urlMatching("/api/users\\.info.*"))
                        .withQueryParam("user", equalTo(feeneyfeeneybobeeney.userId))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )

        assert(response).isNull()
    }

    @Test
    fun `it posts an ephemeral message to avocado sender`() {
        stubFor(post(urlEqualTo("/api/chat.postEphemeral"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postEphemeralMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postSentAvocadoMessage(
                channel = "GENERAL",
                sender = markardito.userId,
                avocadosEach = 1,
                receivers = listOf(feeneyfeeneybobeeney.userId),
                remainingAvocados = 4
        )

        val expectedMessage = "<@$patrick> received 1 avocado from you. You have 4 avocados left to give out today."

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postEphemeral"))
                        .withRequestBody(equalTo("{\"channel\":\"$channel\"," +
                                "\"text\":\"$expectedMessage\"," +
                                "\"user\":\"${markardito.userId}\"" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it posts an ephemeral message to avocado sender when they do not have enough avocados left to give`() {
        stubFor(post(urlEqualTo("/api/chat.postEphemeral"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postEphemeralMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postNotEnoughAvocadosMessage(channel = "GENERAL", sender = markardito.userId, remainingAvocados = 4)

        val expectedMessage = "You only have 4 avocados left to give out today!"

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postEphemeral"))
                        .withRequestBody(equalTo("{\"channel\":\"$channel\"," +
                                "\"text\":\"$expectedMessage\"," +
                                "\"user\":\"${markardito.userId}\"" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test // TODO these tests take a long time. is this one necessary?
    fun `it posts an ephemeral message to avocado sender when they have zero left to give`() {
        stubFor(post(urlEqualTo("/api/chat.postEphemeral"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postEphemeralMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postNotEnoughAvocadosMessage(channel = "GENERAL", sender = markardito.userId, remainingAvocados = 0)

        val expectedMessage = "You have no more avocados left to give out today!"

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postEphemeral"))
                        .withRequestBody(equalTo("{\"channel\":\"$channel\"," +
                                "\"text\":\"$expectedMessage\"," +
                                "\"user\":\"${markardito.userId}\"" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it creates a message listing all avocado receivers`() {
        assert(slackClient.craftAvocadoReceiptMessage(listOf(patrick), 1, 4))
                .isEqualTo("<@$patrick> received 1 avocado from you. You have 4 avocados left to give out today.")

        assert(slackClient.craftAvocadoReceiptMessage(listOf(patrick, jeremy), 1, 3))
                .isEqualTo("<@$patrick> and <@$jeremy> each received 1 avocado from you. You have 3 avocados left to give out today.")

        assert(slackClient.craftAvocadoReceiptMessage(listOf(patrick, jeremy, mark), 1, 2))
                .isEqualTo("<@$patrick>, <@$jeremy>, and <@$mark> each received 1 avocado from you. You have 2 avocados left to give out today.")

        assert(slackClient.craftAvocadoReceiptMessage(listOf(patrick, jeremy), 2, 1))
                .isEqualTo("<@$patrick> and <@$jeremy> each received 2 avocados from you. You have 1 avocado left to give out today.")

        assert(slackClient.craftAvocadoReceiptMessage(listOf(patrick), 5, 0))
                .isEqualTo("<@$patrick> received 5 avocados from you. You have no avocados left to give out today.")
    }

    @Test
    fun `it opens a direct IM channel and sends a direct message to a user`() {
        stubFor(post(urlEqualTo("/api/conversations.open"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(openConversationChannelResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )
        stubFor(post(urlEqualTo("/api/chat.postMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.sendAvocadoReceivedDirectMessage(user = mark, avocadosReceived = 1, sender = patrick)

        verify(
                postRequestedFor(urlEqualTo("/api/conversations.open"))
                        .withRequestBody(equalTo("{" +
                                "\"users\":\"$mark\"" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postMessage"))
                        .withRequestBody(equalTo("{\"channel\":\"$channelId\"," +
                                "\"text\":\"You received 1 avocado from <@$patrick>!\"," +
                                "\"attachments\":[]" +
                                "}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it creates a message`() {
        assert(slackClient.craftAvocadoReceivedMessage(1, patrick)).isEqualTo("You received 1 avocado from <@$patrick>!")

        assert(slackClient.craftAvocadoReceivedMessage(2, patrick)).isEqualTo("You received 2 avocados from <@$patrick>!")
    }

    @Test
    fun `it posts a welcome message`() {
        stubFor(post(urlEqualTo("/api/chat.postMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postWelcomeMessage(channelId)

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postMessage"))
                        .withRequestBody(equalTo(expectedWelcomeMessage))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    private val channel = "GENERAL"
    private val channelId = "D069C7QFK"
    private val token = "iamagoodbot"

    private val postMessageResponse = "{\n" +
            "    \"ok\": true,\n" +
            "    \"channel\": \"$channel\",\n" +
            "    \"ts\": \"1503435956.000247\"\n" +
            "}"

    private val postEphemeralMessageResponse = "{\n" +
            "    \"ok\": true,\n" +
            "    \"message_ts\": \"1502210682.580145\"\n" +
            "}"

    private val openConversationChannelResponse = "{\n" +
            "    \"ok\": true,\n" +
            "    \"channel\":{\"id\":\"$channelId\"}\n" +
            "}"

    private val userInfoResponse = SlackUserResponse(
            ok = true,
            user = SlackUser(
                    id = "",
                    name = "",
                    profile = SlackUserProfile(realName = "", displayName = ""),
                    isBot = false,
                    isRestricted = false,
                    isUltraRestricted = false
            )
    )

    private val userNotFoundResponse = SlackUserResponse(ok = true, error = "user_not_found")

    private val expectedWelcomeMessage = jacksonObjectMapper().writeValueAsString(SlackMessage(
            channel = channelId,
            text = "",
            attachments = listOf(MessageAttachment(
                    title = "How it Works",
                    pretext = "Hola! My name is HolyGuacamole. You can use me to give someone an :avocado: when you'd like to show praise, appreciation, or to add a little happiness to their day.",
                    text = "- Everyone has 5 avocados to give out per day.\n- To give someone an avocado, add an avocado emoji after their username like this: `@username You're a guac star! :avocado:`\n- Avocados are best served with a nice message!\n- You can give avocados to anyone on your team. I am always watching, so you don't need to invite me to your channel unless you want to talk to me.\n- If you want to interact with me directly, you can invite me like this: \n`/invite @holyguacamole`\n- You can see the leaderboard by typing: `@holyguacamole leaderboard`",
                    markdownIn = listOf("text")
            ))
    ))
}
