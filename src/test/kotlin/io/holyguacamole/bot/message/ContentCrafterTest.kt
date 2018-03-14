package io.holyguacamole.bot.message

import assertk.assert
import assertk.assertions.isEqualTo
import io.holyguacamole.bot.MockIds.jeremy
import io.holyguacamole.bot.MockIds.mark
import io.holyguacamole.bot.MockIds.patrick
import io.holyguacamole.bot.message.ContentCrafter.notEnoughAvocados
import io.holyguacamole.bot.message.ContentCrafter.receivedAvocadoMessage
import io.holyguacamole.bot.message.ContentCrafter.sentAvocadoMessage
import org.junit.Test

class ContentCrafterTest {

    @Test
    fun `it crafts the correct sent avocados message`() {
        assert(sentAvocadoMessage(listOf(patrick), 1, 4))
                .isEqualTo("<@${patrick}> received 1 avocado from you. You have 4 avocados left to give out today.")

        assert(sentAvocadoMessage(listOf(patrick, jeremy), 1, 3))
                .isEqualTo("<@${patrick}> and <@${jeremy}> each received 1 avocado from you. You have 3 avocados left to give out today.")

        assert(sentAvocadoMessage(listOf(patrick, jeremy, mark), 1, 2))
                .isEqualTo("<@${patrick}>, <@${jeremy}>, and <@${mark}> each received 1 avocado from you. You have 2 avocados left to give out today.")

        assert(sentAvocadoMessage(listOf(patrick, jeremy), 2, 1))
                .isEqualTo("<@${patrick}> and <@${jeremy}> each received 2 avocados from you. You have 1 avocado left to give out today.")

        assert(sentAvocadoMessage(listOf(patrick), 5, 0))
                .isEqualTo("<@${patrick}> received 5 avocados from you. You have no avocados left to give out today.")
    }

    @Test
    fun `it crafts the correct not enough avocados message`() {
        assert(notEnoughAvocados(0)).isEqualTo("You have no more avocados left to give out today!")
        assert(notEnoughAvocados(1)).isEqualTo("You only have 1 avocado left to give out today!")
        assert(notEnoughAvocados(2)).isEqualTo("You only have 2 avocados left to give out today!")
    }

    @Test
    fun `it crafts the correct received avocados message`() {
        assert(receivedAvocadoMessage(1, patrick,"CTEST123")).isEqualTo("You received 1 avocado from <@$patrick> in <#CTEST123>!")
        assert(receivedAvocadoMessage(2, patrick,"CTEST125")).isEqualTo("You received 2 avocados from <@$patrick> in <#CTEST125>!")
    }
}
