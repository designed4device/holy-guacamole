package io.holyguacamole.bot.helper

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun HGDateTime(date: LocalDate, time: LocalTime) =
        ZonedDateTime.of(date, time, ZoneId.of("America/Chicago"))

fun HGEpochSeconds(date: LocalDate, time: LocalTime) =
        HGDateTime(date, time).withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond()

fun HGEpochSeconds(date: LocalDate) = HGEpochSeconds(date, LocalTime.now())

fun HGEpochSecondsNow() = HGEpochSeconds(LocalDate.now(), LocalTime.now())
