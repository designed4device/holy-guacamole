package io.holyguacamole.web.controller

import io.holyguacamole.bot.message.AvocadoReceiptRepository
import io.holyguacamole.bot.user.UserRepository
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController(val leaderboardService: LeaderboardService) {

    @GetMapping("/")
    fun main(model: Model): String {
        return "welcome"
    }

    @GetMapping("/leaderboard")
    fun getLeaderboard(model: Model): String {
        val leaderboard = leaderboardService.getLeaderboard()

        model.addAttribute("totalCount", leaderboard.totalCount)
        model.addAttribute("leaderboard", leaderboard.leaders)
        return "leaderboard"
    }
}

@Service
class LeaderboardService(val userRepository: UserRepository, val avocadoReceiptRepository: AvocadoReceiptRepository) {

    fun getLeaderboard(): Leaderboard {
        val users = userRepository.findAll()
        val avocadoCounts = avocadoReceiptRepository.getLeaderboard(users.size.toLong())
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