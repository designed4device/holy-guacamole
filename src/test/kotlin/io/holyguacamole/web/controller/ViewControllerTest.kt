package io.holyguacamole.web.controller

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.message.AvocadoReceiptRepository
import io.holyguacamole.bot.user.UserRepository
import org.junit.Before
import org.junit.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.ui.Model
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.spring5.view.ThymeleafViewResolver

class ViewControllerTest {

    private val service: LeaderboardService = mock()
    private val controller = ViewController(service)
    private val mockMvc: MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setViewResolvers(ThymeleafViewResolver().apply { templateEngine = SpringTemplateEngine() })
            .build()
    private val mockModel: Model = mock()

    @Before
    fun setUp() {
        whenever(service.getLeaderboard()).thenReturn(MockLeaderboard.all)
    }

    @Test
    fun `directs index request to welcome template`() {
        mockMvc.perform(get("/")).andExpect(content().string("welcome"))
    }

    @Test
    fun `directs leaderboard request to leaderboard template`() {
        mockMvc.perform(get("/leaderboard")).andExpect(content().string("leaderboard"))
    }

    @Test
    fun `passes count and leaderboard to model`() {
        controller.getLeaderboard(mockModel)
        verify(mockModel).addAttribute("totalCount", MockLeaderboard.all.totalCount)
        verify(mockModel).addAttribute("leaderboard", MockLeaderboard.all.leaders)
    }
}

class LeaderboardServiceTest {

    private val userRepository: UserRepository = mock()
    private val avocadoReceiptRepository: AvocadoReceiptRepository = mock()
    private val service = LeaderboardService(userRepository, avocadoReceiptRepository)

    @Before
    fun setUp() {
        whenever(userRepository.findAll()).thenReturn(MockUsers.all)
        whenever(avocadoReceiptRepository.getLeaderboard(MockAvocadoCounts.all.size.toLong())).thenReturn(MockAvocadoCounts.all)
    }

    @Test
    fun `returns leaderboard`() {
        val leaderboard = service.getLeaderboard()
        assert(leaderboard).isEqualTo(MockLeaderboard.all)
    }
}