package io.holyguacamole.bot.slack

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.holyguacamole.bot.user.User

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackUserResponse(val ok: Boolean, val user: SlackUser? = null, val error: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackUser(val id: String,
                     val name: String,
                     val profile: SlackUserProfile,
                     @JsonProperty("is_bot") val isBot: Boolean,
                     @JsonProperty("is_restricted") val isRestricted: Boolean,
                     @JsonProperty("is_ultra_restricted") val isUltraRestricted: Boolean)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackUserProfile(@JsonProperty("real_name_normalized") val realName: String,
                            @JsonProperty("display_name_normalized") val displayName: String)

fun SlackUser.toUser(): User = User(
        userId = this.id,
        name = this.displayNameOrRealName,
        isBot = this.isBot
)

val SlackUser.displayNameOrRealName
    get() =
        if (this.profile.displayName.isNotEmpty()) {
            this.profile.displayName
        } else {
            this.profile.realName
        }
