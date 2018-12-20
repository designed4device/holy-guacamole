package io.holyguacamole

import io.holyguacamole.bot.message.AvocadoReceipt
import io.holyguacamole.bot.user.User

fun List<Any>.nullifyIds(): List<Any> = this.map {
    when (it) {
        is AvocadoReceipt -> it.copy(id = null)
        is User -> it.copy(id = null)
        else -> it
    }
}

fun List<User>.nullifyUserIds(): List<User> = this.map { it.copy(id = null) }