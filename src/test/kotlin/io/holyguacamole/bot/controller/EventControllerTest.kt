package io.holyguacamole.bot.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockito_kotlin.mock
import io.holyguacamole.bot.MockAppMentions
import io.holyguacamole.bot.MockJoinedChannelEvents
import io.holyguacamole.bot.MockMessages
import io.holyguacamole.bot.MockUrlVerification
import io.holyguacamole.bot.MockUserChangeEvent
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
        mvc.perform(post("/events"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `it receives a challenge and responds success with the challenge value`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockUrlVerification.withCorrectToken))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
                .andExpect(content().string("{\"challenge\":\"somechallenge\"}"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    }

    @Test
    fun `it verifies the token received in requests`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockUrlVerification.withCorrectToken))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)

        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockUrlVerification.withIncorrectToken))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `it receives message events and returns 200`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockMessages.withSingleMentionAndSingleAvocado))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
    }

    @Test
    fun `it returns a 400 when request is not a challenge or message event`() {
        mvc.perform(post("/events")
                .content("{\"token\": \"thisisagoodtoken\", \"type\": \"BAD\"}")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `it receives app_mention events and returns a 200`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockAppMentions.leaderboard))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
    }

    @Test
    fun `it receives user_change events and returns a 200`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockUserChangeEvent.markNameUpdate))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
    }

    @Test
    fun `it receives member_joined_channel events and returns a 200`() {
        mvc.perform(post("/events")
                .content(jacksonObjectMapper().writeValueAsString(MockJoinedChannelEvents.botJoined))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk)
    }
}
