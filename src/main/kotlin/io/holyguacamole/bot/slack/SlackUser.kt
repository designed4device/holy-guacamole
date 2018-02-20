package io.holyguacamole.bot.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.holyguacamole.bot.user.User

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackUser(val id: String,
                     val name: String,
                     @JsonProperty("real_name") val realName: String,
                     @JsonProperty("is_bot") val isBot: Boolean,
                     @JsonProperty("is_restricted") val isRestricted: Boolean,
                     @JsonProperty("is_ultra_restricted") val isUltraRestricted: Boolean)

fun SlackUser.toUser(): User = User(
        id = "",
        userId = this.id,
        name = this.name,
        isBot = this.isBot
)