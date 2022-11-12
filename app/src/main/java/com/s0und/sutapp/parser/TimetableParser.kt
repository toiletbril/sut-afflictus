package com.s0und.sutapp.parser

import com.kizitonwose.calendar.core.atStartOfMonth
import com.s0und.sutapp.data.*
import com.s0und.sutapp.states.DatabaseState
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth

data class UniSubject(
    val subjectName: String,
    val classroom: String,
    val classType: String,
    val teacherName: String,
    val classSchedule1: String, //start time
    val classSchedule2: String,  //end time
)

suspend fun getClassesForSemester(
    groupID: String,
    databaseState: DatabaseState
): Result<Unit> {

    val request = requestSemesterTimetable(groupID)

    return if (request.isSuccess) {
        val page = request.getOrNull()!!
        val classesForSemester = parseClassesSchedule(page)

        parseSubjects(classesForSemester, groupID, databaseState)

        Result.success(Unit)
    } else {
        Result.failure(request.exceptionOrNull()!!)
    }
}

suspend fun parseSubjects(
    classesForSemester: List<UniClass>,
    groupID: String,
    databaseState: DatabaseState
) {

    val currentDate: LocalDate = LocalDate.now()

    val semester = if (currentDate.month.value in 9..12) 1 else 2
    val currentYearMonth: YearMonth = YearMonth.now()

    val startDate: LocalDate = if (semester == 1) YearMonth.of(currentYearMonth.year, 9)
        .atStartOfMonth() else YearMonth.of(currentYearMonth.year, 1).atStartOfMonth()

    val firstWeek = startDate.minusDays(startDate.dayOfWeek.value.toLong())

    val semesterDays = mutableMapOf<String, MutableList<UniSubject>>()

    for (i in classesForSemester.indices) {
        val k = classesForSemester[i]
        for (j in k.weeks) {
            val date = firstWeek.plusDays(k.weekday.toLong() + (7 * (j - 1)))
            val id = "$groupID$date"

            val uniSubject = UniSubject(k.name, k.classroom, k.type, k.teacher, k.startTime, k.endTime)

            if (semesterDays[id] == null) semesterDays[id] = mutableListOf()
            semesterDays[id]!!.add(uniSubject)
        }
    }

    for (semesterDay in semesterDays) {
        val day = UniDay(semesterDay.key, semesterDay.value)
        databaseState.addDay(day)
    }
}

data class UniClass (
    val name: String,
    val type: String,
    val teacher: String,
    val classroom: String,
    val weeks: List<Int>,
    val weekday: Int,
    val startTime: String,
    val endTime: String,
)

private fun parseClassesSchedule(page: Element): List<UniClass> {

    val pairList = mutableListOf<UniClass>()
    val allPairs = page.select(".pair")

    for (i in allPairs.indices) {

        val pair = allPairs[i]

        val pairWeekday = pair.attr("weekday").toInt()
        val pairPosition = pair.attr("pair") //2 - first
        val pairName = pair.select(".subect").text()
        val pairType = pair.select(".type").text()

        val pairStringWeeks = pair.select(".weeks").text()
            .replace("(", "")
            .replace(")", "")
            .replace("н", "")
            .split(", ").toMutableList()
        val pairWeeks = mutableListOf<Int>()
        for (j in pairStringWeeks.indices) {
            pairWeeks.add(pairStringWeeks[j].toInt())
        }

        if (pairName.lowercase() == "военная подготовка") {
            continue
        }

        val pairClassroom = pair.select(".aud").text()
        val classroom = if (pairClassroom.isNotEmpty()) pairClassroom.replace("; Б22", "")
        else "ауд. не указана"

        val classType = when (pairType) {
            "(Практические занятия)" -> "пр"
            "(Лабораторная работа)" -> "лаб"
            "(Лекция)" -> "лек"
            else -> "???"
        }

        val pairTeacher = pair.select(".teacher").text()
        val teacher = pairTeacher.ifEmpty { "Препод. не указан" }

        val classStart = when (pairPosition) {
            "2" -> "09:00"
            "3" -> "10:45"
            "4" -> "13:00"
            "5" -> "14:45"
            "83" -> "9:00"
            "84" -> "10:30"
            "85" -> "12:00"
            "86" -> "13:30"
            "87" -> "15:00"
            "6" -> "16:30"
            "7" -> "18:15"
            "30" -> "20:00"
            else -> "?"
        }

        val classEnd = when (pairPosition) {
            "2" -> "10:35"
            "3" -> "12:20"
            "4" -> "14:35"
            "5" -> "16:20"
            "83" -> "10:30"
            "84" -> "12:00"
            "86" -> "15:00"
            "87" -> "16:30"
            "6" -> "18:05"
            "7" -> "19:50"
            "30" -> "21:35"
            else -> "?"
        }

        pairList.add(UniClass(pairName, classType, teacher, classroom, pairWeeks.toList(), pairWeekday, classStart, classEnd))
    }
    return pairList.toList()
}

private fun requestSemesterTimetable(groupID: String): Result<Element> {
    val nURL = "https://cabinet.sut.ru/raspisanie_all_new"
    val mURL = "$nURL?group=$groupID"
    return try {
        val page = Jsoup.connect(mURL)
            .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.104 (Edition Yx GX)")
            .get().body()
        Result.success(page)
    } catch (e: IOException) { Result.failure(e) }
}
