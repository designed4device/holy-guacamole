package io.holyguacamole.web.controller

import io.holyguacamole.bot.message.AvocadoCount
import io.holyguacamole.bot.user.User
import java.util.Base64

object MockData {
    val jon = Data(name = "Jon Snow", avocados = 300)
    val tyrion = Data(name = "Tyrion Lannister", avocados = 293)
    val arya = Data(name = "Arya Stark", avocados = 176)
    val jaime = Data(name = "Jaime Lannister", avocados = 170)
    val daenerys = Data(name = "Daenerys Targaryen", avocados = 159)
    val sansa = Data(name = "Sansa Stark", avocados = 152)
    val bran = Data(name = "Bran Stark", avocados = 143)
    val eddard = Data(name = "Eddard Stark", avocados = 136)
    val catelyn = Data(name = "Catelyn Stark", avocados = 122)
    val cersei = Data(name = "Cersei Lannister", avocados = 118)

    data class Data(val name: String, val avocados: Int)
}

object MockUsers {
    val jon = MockData.jon.name.toUser()
    val tyrion = MockData.tyrion.name.toUser()
    val arya = MockData.arya.name.toUser()
    val jaime = MockData.jaime.name.toUser()
    val daenerys = MockData.daenerys.name.toUser()
    val sansa = MockData.sansa.name.toUser()
    val bran = MockData.bran.name.toUser()
    val eddard = MockData.eddard.name.toUser()
    val catelyn = MockData.catelyn.name.toUser()
    val cersei = MockData.cersei.name.toUser()

    val all = listOf(jon, tyrion, arya, jaime, daenerys, sansa, bran, eddard, catelyn, cersei)

    private fun String.toUser(): User =
        User(userId = String(Base64.getEncoder().encode(this.toByteArray())), name = this, isBot = false)
}

object MockLeaderboard {
    val jon = Leader(name=MockUsers.jon.name, count=MockData.jon.avocados)
    val tyrion = Leader(name=MockUsers.tyrion.name, count=MockData.tyrion.avocados)
    val arya = Leader(name=MockUsers.arya.name, count=MockData.arya.avocados)
    val jaime = Leader(name=MockUsers.jaime.name, count=MockData.jaime.avocados)
    val daenerys = Leader(name=MockUsers.daenerys.name, count=MockData.daenerys.avocados)
    val sansa = Leader(name=MockUsers.sansa.name, count=MockData.sansa.avocados)
    val bran = Leader(name=MockUsers.bran.name, count=MockData.bran.avocados)
    val eddard = Leader(name=MockUsers.eddard.name, count=MockData.eddard.avocados)
    val catelyn = Leader(name=MockUsers.catelyn.name, count=MockData.catelyn.avocados)
    val cersei = Leader(name=MockUsers.cersei.name, count=MockData.cersei.avocados)

    val all = Leaderboard(totalCount = 10, leaders = listOf(jon, tyrion, arya, jaime, daenerys, sansa, bran, eddard, catelyn, cersei))
}

object MockAvocadoCounts {
    val jon = AvocadoCount(receiver= MockUsers.jon.userId, count=MockData.jon.avocados)
    val tyrion = AvocadoCount(receiver= MockUsers.tyrion.userId, count=MockData.tyrion.avocados)
    val arya = AvocadoCount(receiver= MockUsers.arya.userId, count=MockData.arya.avocados)
    val jaime = AvocadoCount(receiver= MockUsers.jaime.userId, count=MockData.jaime.avocados)
    val daenerys = AvocadoCount(receiver= MockUsers.daenerys.userId, count=MockData.daenerys.avocados)
    val sansa = AvocadoCount(receiver= MockUsers.sansa.userId, count=MockData.sansa.avocados)
    val bran = AvocadoCount(receiver= MockUsers.bran.userId, count=MockData.bran.avocados)
    val eddard = AvocadoCount(receiver= MockUsers.eddard.userId, count=MockData.eddard.avocados)
    val catelyn = AvocadoCount(receiver= MockUsers.catelyn.userId, count=MockData.catelyn.avocados)
    val cersei = AvocadoCount(receiver= MockUsers.cersei.userId, count=MockData.cersei.avocados)

    val all = listOf(jon, tyrion, arya, jaime, daenerys, sansa, bran, eddard, catelyn, cersei)
}
