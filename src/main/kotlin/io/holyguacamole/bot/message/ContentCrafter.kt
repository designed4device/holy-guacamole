package io.holyguacamole.bot.message

object ContentCrafter {

    const val BOT_NAME = "@holyguacamole"
    const val AVOCADO_TEXT = ":avocado:"
    const val TACO_TEXT = ":taco:"

    const val WELCOME_TITLE = "How it Works"

    const val WELCOME_PRETEXT = "Hola! My name is HolyGuacamole. " +
            "You can use me to give someone an $AVOCADO_TEXT when you'd like to show praise, " +
            "appreciation, or to add a little happiness to their day."

    const val WELCOME_TEXT =
            """- Everyone has 5 avocados to give out per day.
- To give someone an avocado, add an avocado emoji after their username like this: `@username You're a guac star! $AVOCADO_TEXT`
- Avocados are best served with a nice message!
- You can give avocados to anyone on your team. I am always watching, so you don't need to invite me to your channel unless you want to talk to me.
- If you want to interact with me directly, you can invite me like this:
`/invite $BOT_NAME`
- You can see the leaderboard by typing: `$BOT_NAME leaderboard`"""

    const val AVOCADO_REMINDER = "Well, this is guacward! Did you mean to send an $AVOCADO_TEXT?"

    val welcomeMessage = MessageAttachment(
            title = WELCOME_TITLE,
            pretext = WELCOME_PRETEXT,
            text = WELCOME_TEXT,
            markdownIn = listOf("text")
    )


    fun notEnoughAvocados(remainingAvocados: Int): String =
            when (remainingAvocados) {
                0 -> "You have no more avocados left to give out today!"
                else -> "You only have $remainingAvocados ${"avocado".pluralize(remainingAvocados)} left to give out today!"
            }

    fun receivedAvocadoMessage(avocadosReceived: Int, sender: String): String =
            when (avocadosReceived) {
                1 -> "You received 1 avocado from ${sender.asMention()}!"
                else -> "You received $avocadosReceived avocados from ${sender.asMention()}!"
            }

    fun sentAvocadoMessage(receivers: List<String>, avocadosEach: Int, remainingAvocados: Int): String {

        val receiversString = when (receivers.size) {
            1 -> receivers.first().asMention()
            2 -> receivers.joinToString(separator = " and ") { it.asMention() }
            else -> receivers.joinToString(
                    separator = ", ",
                    limit = receivers.size - 1,
                    truncated = "and ${receivers.last().asMention()}"
            ) { it.asMention() }
        }

        return "$receiversString ${if (receivers.size > 1) "each " else ""}" +
                "received $avocadosEach ${"avocado".pluralize(avocadosEach)} from you. " +
                "You have ${if (remainingAvocados == 0) "no" else "$remainingAvocados"} ${"avocado".pluralize(remainingAvocados)} " +
                "left to give out today."
    }

    private fun String.pluralize(n: Int): String = if (n != 1) "${this}s" else this
    private fun String.asMention(): String = "<@$this>"
}

