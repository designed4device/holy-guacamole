package io.holyguacamole.bot.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    private val slackRequest = ChallengeRequest(
            token = "thisisagoodtoken",
            challenge = "somechallenge",
            type = "url_verification"
    )

    @Test
    fun `it receives messages and returns 400 error when no body is present`() {
        mvc.perform(post("/messages"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `it receives a challenge and responds success with the challenge value`() {
        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(slackRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
                .andExpect(content().string("{\"challenge\":\"somechallenge\"}"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    }

    @Test
    fun `it verifies the token received in requests`() {
        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(slackRequest))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)

        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(slackRequest.copy(token = "verybadtoken")))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized)
    }
}
