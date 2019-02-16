package io.holyguacamole.bot.user

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val mongoRepository: UserMongoRepository, private val mongoTemplate: MongoTemplate) {

    fun findAll(): List<User> = mongoRepository.findAll()

    fun saveAll(entities: Iterable<User>): List<User> =
            mongoRepository.saveAll(entities.map { it.copy() })

    fun save(entity: User): User = mongoRepository.save(entity.copy())

    fun deleteAll() = mongoRepository.deleteAll()

    fun deleteByUserId(userId: String): Int = mongoRepository.deleteByUserId(userId)

    fun findByUserId(userId: String): User? = mongoRepository.findByUserId(userId)

    fun update(user: User) {
        findByUserId(user.userId)?.run {
            save(user.copy(id = this.id))
        }
    }
}

@Repository
interface UserMongoRepository : MongoRepository<User, String> {
    fun deleteByUserId(userId: String): Int

    fun findByUserId(userId: String): User?
}

data class User(val id: String? = null,
                val userId: String,
                val name: String,
                val isBot: Boolean
)
