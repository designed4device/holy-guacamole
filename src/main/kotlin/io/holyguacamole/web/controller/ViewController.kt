package io.holyguacamole.web.controller

import io.holyguacamole.bot.message.AvocadoReceiptRepository
import io.holyguacamole.bot.user.UserRepository
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ViewController(val leaderboardService: LeaderboardService) {

    @GetMapping("/")
    fun main(model: Model): String {
        return "welcome"
    }

    @GetMapping("/leaderboard")
    fun getLeaderboard(model: Model): String {
        val leaderboard = leaderboardService.getLeaderboard()

        model.addAttribute("year", "")
        model.addAttribute("totalCount", leaderboard.totalCount)
        model.addAttribute("leaderboard", leaderboard.leaders)
        return "leaderboard"
    }

    @GetMapping("/leaderboard/{year}")
    fun getLeaderboardByYear(model: Model, @PathVariable("year") year: Int): String {
        val leaderboard = leaderboardService.getLeaderboard(year = year)

        model.addAttribute("year", year)
        model.addAttribute("totalCount", leaderboard.totalCount)
        model.addAttribute("leaderboard", leaderboard.leaders)
        return "leaderboard"
    }
}

@Service
class LeaderboardService(val userRepository: UserRepository, val avocadoReceiptRepository: AvocadoReceiptRepository) {

    fun getLeaderboard(year: Int = 0): Leaderboard {
        val users = userRepository.findAll()
        val avocadoCounts = avocadoReceiptRepository.getLeaderboard(limit = 0, year = year)
        return Leaderboard(
            totalCount = avocadoCounts.size,
            leaders = avocadoCounts.map { avocadoCount ->
                Leader(
                    name = users.find { it.userId == avocadoCount.receiver }?.name ?: "Unknown",
                    count = avocadoCount.count
                )
            }
        )
    }
}

data class Leaderboard(
    val totalCount: Int,
    val leaders: List<Leader>
)

data class Leader(
    val name: String,
    val count: Int
)