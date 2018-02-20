package io.holyguacamole.bot.user

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.holyguacamole.bot.MockSlackUsers.jeremySlack
import io.holyguacamole.bot.MockUsers.eightRib
import io.holyguacamole.bot.MockUsers.feeneyfeeneybobeeney
import io.holyguacamole.bot.MockUsers.jeremyskywalker
import io.holyguacamole.bot.MockUsers.markardito
import io.holyguacamole.bot.message.SlackClient
import io.holyguacamole.bot.slack.SlackUser
import io.holyguacamole.bot.slack.SlackUserProfile
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserServiceTest {

    private val repository = mock<UserRepository>()
    private val slackClient = mock<SlackClient> {
        on { getUserInfo(any()) } doReturn jeremySlack
    }
    private val service = UserService(repository, slackClient)

    @Before
    fun setUp() {
        repository.saveAll(listOf(markardito, feeneyfeeneybobeeney))
    }

    @After
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `it replaces a user`() {
        service.replace(eightRib)

        verify(repository).deleteByUserId(markardito.userId)
        verify(repository).save(eightRib)
    }

    @Test
    fun `it finds a user by userId`() {
        service.findByUserIdOrGetFromSlack(feeneyfeeneybobeeney.userId)

        verify(repository).findByUserId(feeneyfeeneybobeeney.userId)
    }

    @Test
    fun `if it can't find a user it uses the Slack API to save them`() {
        val user = service.findByUserIdOrGetFromSlack(jeremyskywalker.userId)

        verify(repository).findByUserId(jeremyskywalker.userId)

        verify(slackClient).getUserInfo(jeremyskywalker.userId)

        assert(user).isEqualTo(jeremyskywalker)
    }

    @Test
    fun `it uses real name if user doesn't have a display name`() {
        val slackUserWithoutDisplayName = SlackUser(
                id = "USER",
                name = "jeremy_kapler",
                profile = SlackUserProfile(realName = "Jeremy Kapler", displayName = ""),
                isBot = false,
                isRestricted = false,
                isUltraRestricted = false
        )
        whenever(slackClient.getUserInfo("USER")).thenReturn(slackUserWithoutDisplayName)

        val user: User? = service.findByUserIdOrGetFromSlack("USER")

        assert(user?.name).isEqualTo("Jeremy Kapler")
    }

    @Test
    fun `it adds the user to the repository when it gets them from the Slack API`() {
        service.findByUserIdOrGetFromSlack(jeremyskywalker.userId)

        verify(repository).findByUserId(jeremyskywalker.userId)

        verify(slackClient).getUserInfo(jeremyskywalker.userId)
    }
}
