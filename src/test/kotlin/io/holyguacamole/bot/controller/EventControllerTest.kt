package io.holyguacamole.bot.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockito_kotlin.mock
import io.holyguacamole.bot.MockMessages
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class EventControllerTest {

    private val mvc: MockMvc = MockMvcBuilders
            .standaloneSetup(EventController("thisisagoodtoken", mock()))
            .build()

    @Test
    fun `it receives messages and returns 400 error when no body is present`() {
        mvc.perform(post("/messages"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `it receives a challenge and responds success with the challenge value`() {
        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(MockMessages.challenge))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
                .andExpect(content().string("{\"challenge\":\"somechallenge\"}"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    }

    @Test
    fun `it verifies the token received in requests`() {
        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(MockMessages.challenge))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)

        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(MockMessages.challenge.copy(token = "verybadtoken")))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `it receives message events and returns 200`() {
        mvc.perform(post("/messages")
                .content(jacksonObjectMapper().writeValueAsString(MockMessages.withSingleMentionAndAvocado))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
    }

    @Test
    fun `it returns a 400 when request is not a challenge or message event`() {
        mvc.perform(post("/messages")
                .content("{\"token\": \"thisisagoodtoken\", \"type\": \"BAD\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest)
    }
}
