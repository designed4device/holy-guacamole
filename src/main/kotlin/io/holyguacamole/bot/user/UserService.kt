package io.holyguacamole.bot.user

import io.holyguacamole.bot.message.SlackClient
import io.holyguacamole.bot.slack.toUser
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository,
                  private val slackClient: SlackClient) {

    fun replace(user: User) {
        repository.update(user)
    }

    fun findByUserIdOrGetFromSlack(userId: String): User? =
            findByUserId(userId) ?: getFromSlack(userId)

    private fun findByUserId(userId: String): User? = repository.findByUserId(userId)

    private fun getFromSlack(userId: String): User? =
            slackClient.getUserInfo(userId)
                    ?.toUser()
                    ?.also { repository.save(it) }
}
