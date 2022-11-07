package com.s0und.sutapp.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.s0und.sutapp.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun getFormattedWeek(day: LocalDate): String {

    val firstDayDate = day.minusDays(getCurrentRelativeDayIndex(day).toLong())
    val lastDayDate = firstDayDate.plusDays(6)
    val firstMonth = if (firstDayDate.month != lastDayDate.month) " ${firstDayDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}" else ""
    val lastMonth = if (firstDayDate.month != lastDayDate.month) " ${lastDayDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}" else " ${firstDayDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}"

    return "${firstDayDate.dayOfMonth}$firstMonth ${stringResource(R.string.TO)} ${lastDayDate.dayOfMonth}$lastMonth".lowercase()
}

fun getCurrentRelativeDayIndex(date: LocalDate): Int {
    val day = date.dayOfWeek.value
    return day - 1
}

fun openGithubURL(context: Context) {
    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/toiletbril/sut-afflictus"))
    context.startActivity(urlIntent)
}

fun openClassroomURL(classRoom: String, context: Context) {
    var urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nav.sut.ru/"))
    if (classRoom.startsWith("ауд.: ")) {
        val classroom = classRoom.substring(6).dropLast(2)
        val k = classRoom.last()
        urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nav.sut.ru/?cab=k$k-$classroom"))
    }
    context.startActivity(urlIntent)
}
