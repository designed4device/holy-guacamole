package io.holyguacamole.bot.message

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.holyguacamole.bot.MockLeaderboards
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.user.User
import io.holyguacamole.bot.user.UserRepository
import io.holyguacamole.bot.user.UserService
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

        slackClient.postLeaderboard("GENERAL", mapOf(feeneyfeeneybobeeney to 3, jeremyskywalker to 2, markardito to 1))

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

    private val channel = "GENERAL"
    private val token = "iamagoodbot"

    private val postMessageResponse = "{\n" +
            "    \"ok\": true,\n" +
            "    \"channel\": \"$channel\",\n" +
            "    \"ts\": \"1503435956.000247\"\n" +
            "}"
}
