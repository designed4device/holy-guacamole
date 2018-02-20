package io.holyguacamole.bot.user

import io.holyguacamole.bot.message.SlackClient
import io.holyguacamole.bot.slack.toUser
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository,
                  private val slackClient: SlackClient) {

    fun replace(user: User) {
        repository.deleteByUserId(user.userId)
        repository.save(user)
    }

    fun findByUserIdOrGetFromSlack(userId: String): User =
            findByUserId(userId) ?: slackClient.getUserInfo(userId).toUser()

    private fun findByUserId(userId: String): User? = repository.findByUserId(userId)
}
