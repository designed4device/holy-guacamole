package io.holyguacamole.bot.user

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.holyguacamole.bot.MockIds
import io.holyguacamole.bot.MockUsers
import io.holyguacamole.nullifyIds
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private lateinit var mongoRepository : UserMongoRepository

    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        repository = UserRepository(mongoRepository)
    }

    @After
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `it stores the user`() {
        repository.saveAll(listOf(MockUsers.markardito))

        assert(repository.findAll().nullifyIds()).containsExactly(MockUsers.markardito)
    }

    @Test
    fun `it deletes all users`() {
        repository.saveAll(listOf(MockUsers.markardito))
        repository.deleteAll()

        assert(repository.findAll()).isEmpty()
    }

    @Test
    fun `it deletes by userId`() {
        repository.saveAll(listOf(MockUsers.markardito))

        val response = repository.deleteByUserId(MockIds.mark)

        assert(response).isEqualTo(1)

        assert(repository.findAll()).isEmpty()
    }

    @Test
    fun `it finds by userId`() {
        repository.saveAll(listOf(MockUsers.feeneyfeeneybobeeney))

        val response = repository.findByUserId(MockIds.patrick)

        assert(response).isNotNull()

        assert(response?.name).isEqualTo(MockUsers.feeneyfeeneybobeeney.name)
    }
}
