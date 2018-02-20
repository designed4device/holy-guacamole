package io.holyguacamole.bot.controller

data class UrlVerificationResponse(val challenge: String) : SlackResponse
data class EventCallbackResponse(val succeeded: Boolean): SlackResponse
interface SlackResponse
