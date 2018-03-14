package io.holyguacamole.bot.message

import assertk.assert
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
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import io.holyguacamole.bot.slack.SlackUserResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun `it posts a message to the Slack API`() {
        stubFor(post(urlEqualTo("/api/chat.postMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postMessage(
                channel = channel,
                text = "message text",
                attachments = listOf(MessageAttachment(
                        title = "attachment title",
                        pretext = "attachment pretext",
                        text = "attachment text",
                        markdownIn = listOf("pretext", "text")
                ))
        )

        val expectedRequest = "{\"channel\":\"$channel\"," +
                "\"text\":\"message text\"," +
                "\"attachments\":[{" +
                "\"title\":\"attachment title\"," +
                "\"pretext\":\"attachment pretext\"," +
                "\"text\":\"attachment text\"," +
                "\"mrkdwn_in\":[\"pretext\",\"text\"]}" +
                "]}"
        verify(
                postRequestedFor(urlEqualTo("/api/chat.postMessage"))
                        .withRequestBody(equalTo(expectedRequest))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )
    }

    @Test
    fun `it posts an ephemeral message to the Slack API`() {
        stubFor(post(urlEqualTo("/api/chat.postEphemeral"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(postEphemeralMessageResponse)
                        .withHeader("Content-Type", "application/json")
                )
        )

        slackClient.postEphemeralMessage(channel = channel, user = "user", text = "message text")

        val expectedRequest = "{\"channel\":\"$channel\"," +
                "\"text\":\"message text\"," +
                "\"user\":\"user\"" +
                "}"

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postEphemeral"))
                        .withRequestBody(equalTo(expectedRequest))
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
    fun `it sends a direct message to a user`() {
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

        slackClient.sendDirectMessage(user = mark, text = "message text", attachment = "")

        verify(
                postRequestedFor(urlEqualTo("/api/conversations.open"))
                        .withRequestBody(equalTo("{\"users\":\"$mark\"}"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer $token"))
                        .withHeader("Accept", equalTo("application/json"))
        )

        val expectedPostMessageResponse = "{\"channel\":\"$channelId\"," +
                "\"text\":\"message text\"," +
                "\"attachments\":[]" +
                "}"

        verify(
                postRequestedFor(urlEqualTo("/api/chat.postMessage"))
                        .withRequestBody(equalTo(expectedPostMessageResponse))
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
}
