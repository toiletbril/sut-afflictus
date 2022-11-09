package com.s0und.sutapp.ui.timetableScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R
import com.s0und.sutapp.data.openClassroomURL
import com.s0und.sutapp.parser.UniSubject
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SubjectList(subjects: List<UniSubject>, viewModel: TimetableState, modifier: Modifier = Modifier) {
    if (subjects.isEmpty()) NoPairsToday(modifier)
    else {
        LazyColumn(modifier = modifier
            .background(MaterialTheme.colors.background),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) { items(subjects) { SubjectCard(it, viewModel, modifier) }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubjectCard(uniSubject: UniSubject, viewModel: TimetableState, modifier: Modifier = Modifier, date: LocalDate = viewModel.currentDate.value, nowTime: LocalTime = viewModel.currentTime.value) {

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.selectedDayDate.value) {
        expanded = false
    }

    val day = viewModel.selectedDayDate.value

    val isToday = date == day
    val formatter = DateTimeFormatter.ofPattern("H:mm")

    val classStartTime = LocalTime.parse(uniSubject.classSchedule1, formatter)
    val classEndTime = LocalTime.parse(uniSubject.classSchedule2, formatter)

    val isClassNow = classStartTime < nowTime && nowTime < classEndTime && isToday

    val classTodayAndEnded = isToday && classEndTime < nowTime

    Card(backgroundColor = MaterialTheme.colors.surface, elevation = 0.dp,
        modifier = modifier
            .padding(start = 26.dp, end = 26.dp, bottom = 6.dp
            ),
        onClick = { expanded = !expanded },
    ) {

        Column {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                SubjectInfo(
                    uniSubject.subjectName,
                    uniSubject.teacherName,
                    uniSubject.classroom,
                    uniSubject.classSchedule1,
                    uniSubject.classSchedule2,
                    isClassNow,
                    classTodayAndEnded,
                    modifier
                )
                Spacer(modifier.weight(1f))
                ClassType(
                    uniSubject.classType,
                    modifier
                )
            }

            AnimatedVisibility(expanded || isClassNow) {
                Row {
                    SubjectDescription(uniSubject.classroom, modifier)
                    Spacer(modifier.weight(1f))
                    if (isClassNow) ClassTimeLeft(nowTime, classEndTime, modifier)
                }
            }
        }
    }
}

@Composable
fun ClassTimeLeft(now: LocalTime, end: LocalTime, modifier: Modifier) {
    val minutesLeft = (end.toSecondOfDay() - now.toSecondOfDay()).floorDiv(60)
    Text(
        modifier = modifier
            .padding(end = 18.dp),
        text = "$minutesLeft ${stringResource(R.string.MINUTES_UNTIL_END)}",
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.secondaryVariant,
        style = MaterialTheme.typography.body2,
    )
}

@Composable
fun ClassSchedule(classSchedule1: String, classSchedule2: String, modifier: Modifier = Modifier) {
    Row {
        Column(modifier = modifier
            .padding(top = 1.dp, end = 12.dp)
            .align(Alignment.CenterVertically)
        )
        {
            Text (
                text = classSchedule1,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.secondaryVariant
            )
            Text (
                text = classSchedule2,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(top = 2.dp),
                color = MaterialTheme.colors.secondaryVariant
            )
        } } }

@Composable
fun SubjectInfo(
    subjectName: String,
    teacherName: String,
    classRoom: String,
    classSchedule1: String,
    classSchedule2: String,
    isClassNow: Boolean,
    classTodayAndEnded: Boolean,
    modifier: Modifier = Modifier,
) {

    ClassSchedule(classSchedule1, classSchedule2)

    Column {
        Text (
            text = "$subjectName, $classRoom",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            color = if (isClassNow) MaterialTheme.colors.primaryVariant else if (classTodayAndEnded) MaterialTheme.colors.secondaryVariant else MaterialTheme.colors.onSurface,
            modifier = modifier.requiredWidth(205.dp)
        )
        Text (
            modifier = modifier
                .padding(top = 4.dp)
                .requiredWidth(145.dp),
            text = teacherName,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colors.secondaryVariant
        )
    } }

@Composable
fun ClassType(classType: String, modifier: Modifier = Modifier) {
    val color = when (classType) {
        "пр" -> BonchOrange
        "лек" -> BonchYellow
        "лаб" -> BonchRed
        else -> BonchOrange
    }
    val text = when (classType) {
        "пр" -> stringResource(R.string.PRACTICE_SHORT)
        "лек" -> stringResource(R.string.LECTURE_SHORT)
        "лаб" -> stringResource(R.string.LABWORK_SHORT)
        else -> "???"
    }
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .requiredWidthIn(40.dp, 65.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.background,
                modifier = modifier
                    .padding(13.dp, 4.dp)
            )
        }
    }

@Composable
fun SubjectDescription(classRoom: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Box {
        Text(
            modifier = modifier
                .padding(start = 17.dp, bottom = 12.dp)
                .clickable { openClassroomURL(classRoom, ctx) },
            color = BonchBlue,
            text = stringResource(R.string.FIND_CLASSROOM),
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.body2,
        )
    }
}
