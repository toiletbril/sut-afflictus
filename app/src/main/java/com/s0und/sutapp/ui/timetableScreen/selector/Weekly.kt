package com.s0und.sutapp.ui.timetableScreen.selector

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.s0und.sutapp.data.NoRippleInteractionSource
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.theme.BonchBlue
import com.s0und.sutapp.ui.theme.SlightBonchBlue
import com.s0und.sutapp.ui.timetableScreen.PairCountIndicatorRow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun WeeklySelector(viewModel: TimetableState) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val noRipple = NoRippleInteractionSource()

    val firstDayOfWeek = remember { DayOfWeek.MONDAY }

    val state = rememberWeekCalendarState(
        startDate = viewModel.startDate,
        endDate = viewModel.endDate,
        firstVisibleWeekDate = viewModel.getfirstVisibleWeekDate(),
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(state.firstVisibleWeek) {
        viewModel.changeSelectedDate(YearMonth.from(state.firstVisibleWeek.days[6].date))
    }

    WeekCalendar(
        calendarScrollPaged = false,
        state = state,
        dayContent = { day ->
            WeeklySelectorDay(day,
                screenWidth,
                noRipple,
                viewModel
            ) {
                viewModel.changeSelectedDay(it)
            }
        })
}

@Composable
private fun WeeklySelectorDay(
    day: WeekDay,
    screenWidth: Dp,
    noRipple: NoRippleInteractionSource,
    viewModel: TimetableState,
    click: (LocalDate) -> Unit) {

    val today by remember { derivedStateOf { day.date == viewModel.currentDate.value } }
    val selected by remember { derivedStateOf { day.date == viewModel.selectedDayDate.value } }

    val inactive = day.position == WeekDayPosition.OutDate || day.position == WeekDayPosition.InDate

    val colors = MaterialTheme.colors
    val colorAnimation by animateColorAsState(
        if (!selected) colors.background else SlightBonchBlue,
        tween(100, 0, LinearEasing)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.secondaryVariant,
            fontWeight = FontWeight.Medium,
            text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).lowercase(),
        )
            Button(
                onClick = { click(day.date) },
                colors = ButtonDefaults
                    .buttonColors(
                        contentColor = colors.onSurface,
                        backgroundColor = colorAnimation,
                        disabledContentColor = colors.primary,
                        disabledBackgroundColor = colorAnimation
                    ),
                elevation = ButtonDefaults.elevation(0.dp),
                border = BorderStroke(4.dp, if (today) BonchBlue else Color.Transparent),
                shape = CircleShape,
                enabled = if (inactive) false else !selected,
                interactionSource = noRipple,
                modifier = Modifier
                    .size(58.dp)
                    .width(screenWidth / 9)
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (inactive) colors.secondaryVariant else if (selected) colors.surface else colors.onSurface,
                    fontWeight = if (inactive) FontWeight.Normal else FontWeight.Bold,
                )
            }
        if (!inactive) PairCountIndicatorRow(day.date, viewModel)
    }
}
