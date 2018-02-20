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
import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SlackClientTest {

    @Rule
    @JvmField
    val wireMockRule = WireMockRule(8089)

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
                                "\"text\":\"${feeneyfeeneybobeeney.name}: 3\\n${jeremyskywalker.name}: 2\\n${markardito.name}: 1\"" +
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

    private val channel = "GENERAL"
    private val token = "iamagoodbot"

    private val postMessageResponse = "{\n" +
            "    \"ok\": true,\n" +
            "    \"channel\": \"$channel\",\n" +
            "    \"ts\": \"1503435956.000247\"\n" +
            "}"

    private val userInfoResponse = SlackUserResponse(
            ok = true,
            user = SlackUser(
                    id = "",
                    name = "",
                    realName = "",
                    isBot = false,
                    isRestricted = false,
                    isUltraRestricted = false
            )
    )

    private val userNotFoundResponse = SlackUserResponse(ok = true, error = "user_not_found")
}
