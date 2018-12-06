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
- You can give avocados to anyone on your team. I am always watching public channels, so you don't need to invite me unless you want to talk to me.
- I do not watch private channels unless invited, so you will need to invite me to send avocados in a private channel.
- If you want to interact with me directly, you can invite me like this:
`/invite $BOT_NAME`
- You can see the leaderboard by typing: `$BOT_NAME leaderboard`"""

    const val HELP_DIRECTIONS_TITLE = "Directions"
    const val HELP_HOWTO_TITLE = "How To Send Avocados"
    const val HELP_COMMANDS_TITLE = "Commands"

    const val HELP_DIRECTIONS_TEXT =
            """Everyone has 5 avocados to give out per day.
To give someone an avocado, send a message with their username and an avocado.
Avocados are best served with a nice message!
You can give avocados to anyone on your team. I am always watching, so you don't need to invite me to your channel unless you want to talk to me."""

    const val HELP_HOWTO_TEXT =
            """`@username You're a guac star! $AVOCADO_TEXT` Gives 1 avocado to @username
`@username $AVOCADO_TEXT $AVOCADO_TEXT` Gives 2 avocados to @username
`@username @anotherusername $AVOCADO_TEXT $AVOCADO_TEXT` Gives 2 avocados each to @username and @anotherusername
`@username $AVOCADO_TEXT @anotherusername $AVOCADO_TEXT` Gives 2 avocados each to @username and @anotherusername"""

    const val HELP_COMMANDS_TEXT =
"""`/invite $BOT_NAME`: to invite me to a channel
`$BOT_NAME help`: to see helpful resources and information
`$BOT_NAME leaderboard [number]`: to show the leaderboard, eg. leaderboard 20, defaults to top 10"""

    const val HELP_DM_TITLE = "Direct Message Commands"
    const val HELP_DM_TEXT =
"""`help`: to see helpful resources and information
`leaderboard [number]`: to show the leaderboard, eg. leaderboard 20, defaults to top 10
`avocados`: to see how many avocados you have left to give out today"""

    const val AVOCADO_REMINDER = "Well, this is guacward! Did you mean to send an $AVOCADO_TEXT?"

    const val GUACWARD_MESSAGE = "I don't mean to sound _guacward_ but did you mean to use one of these commands?"

    val welcomeMessage = MessageAttachment(
            title = WELCOME_TITLE,
            pretext = WELCOME_PRETEXT,
            text = WELCOME_TEXT,
            markdownIn = listOf(MARKDOWN.TEXT)
    )

    val commandsMessage = MessageAttachment(
            title = HELP_COMMANDS_TITLE,
            pretext = "",
            text = HELP_COMMANDS_TEXT,
            markdownIn = listOf(MARKDOWN.TEXT)
    )

    val helpMessage = listOf(
            MessageAttachment(
                    title = HELP_DIRECTIONS_TITLE,
                    pretext = "",
                    text = HELP_DIRECTIONS_TEXT,
                    markdownIn = listOf(MARKDOWN.TEXT)
            ),
            MessageAttachment(
                    title = HELP_HOWTO_TITLE,
                    pretext = "",
                    text = HELP_HOWTO_TEXT,
                    markdownIn = listOf(MARKDOWN.TEXT)
            ),
            MessageAttachment(
                    title = HELP_DM_TITLE,
                    pretext = "",
                    text = HELP_DM_TEXT,
                    markdownIn = (listOf(MARKDOWN.TEXT))
            ),
            commandsMessage
    )

    fun notEnoughAvocados(remainingAvocados: Int): String =
            when (remainingAvocados) {
                0 -> "You have no more avocados left to give out today!"
                else -> "You only have $remainingAvocados ${"avocado".pluralize(remainingAvocados)} left to give out today!"
            }

    fun receivedAvocadoMessage(avocadosReceived: Int, sender: String, channel: String): String =
            when (avocadosReceived) {
                1 -> "You received 1 avocado from ${sender.asMention()} in ${channel.asChannelMention()}!"
                else -> "You received $avocadosReceived avocados from ${sender.asMention()} in ${channel.asChannelMention()}!"
            }

    fun sentAvocadoMessage(receivers: List<String>, avocadosEach: Int, remainingAvocados: Int): String {

        val receiversString = listReceivers(receivers)

        return "$receiversString ${if (receivers.size > 1) "each " else ""}" +
                "received $avocadosEach ${"avocado".pluralize(avocadosEach)} from you. " +
                avocadosLeft(remainingAvocados)
    }

    fun listReceivers(receivers: List<String>): String =
            when (receivers.size) {
                1 -> receivers.first().asMention()
                2 -> receivers.joinToString(separator = " and ") { it.asMention() }
                else -> receivers.joinToString(
                        separator = ", ",
                        limit = receivers.size - 1,
                        truncated = "and ${receivers.last().asMention()}"
                ) { it.asMention() }
            }

    fun avocadosLeft(remainingAvocados: Int): String {
        return "You have ${if (remainingAvocados == 0) "no" else "$remainingAvocados"} ${"avocado".pluralize(remainingAvocados)} " +
                "left to give out today."
    }

    fun revokedAvocadoMessageForSender(revokedAvocadosPerMention: Int, mentions: List<String>, remainingAvocados: Int): String =
            "You revoked $revokedAvocadosPerMention ${"avocado".pluralize(revokedAvocadosPerMention)} from ${listReceivers(mentions)}. ${avocadosLeft(remainingAvocados)}"

    fun revokedAvocadoMessageForReceiver(sender: String, avocadosRevoked: Int, channel: String): String =
            "${sender.asMention()} revoked $avocadosRevoked ${"avocado".pluralize(avocadosRevoked)} you previously received in ${channel.asChannelMention()}"

    private fun String.pluralize(n: Int): String = if (n != 1) "${this}s" else this
    private fun String.asMention(): String = "<@$this>"
    private fun String.asChannelMention(): String = "<#$this>"
}


