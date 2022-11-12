package com.s0und.sutapp.ui.timetableScreen.selector

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
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
fun MonthlySelector(viewModel: TimetableState) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val noRipple = NoRippleInteractionSource()

    val state = rememberCalendarState(
        startMonth = YearMonth.from(viewModel.startDate),
        endMonth = YearMonth.from(viewModel.endDate),
        firstVisibleMonth = YearMonth.from(viewModel.getfirstVisibleWeekDate()),
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfGrid
    )

    LaunchedEffect(state.firstVisibleMonth.yearMonth) {
        viewModel.changeSelectedDate(state.firstVisibleMonth.yearMonth)
    }


    HorizontalCalendar(
        modifier = Modifier.requiredHeight(400.dp),
        calendarScrollPaged = true,
        state = state,
        dayContent = { day ->
            MonthlySelectorDay(day,
                screenWidth,
                viewModel,
                noRipple,
            ) {
                viewModel.changeSelectedDay(it)
            }
        },
        monthHeader = {
            MonthHeader(daysOfWeek(DayOfWeek.MONDAY)) }
    )
}

@Composable
private fun MonthHeader(
    daysOfWeek: List<DayOfWeek>,
) {
    Row(Modifier.fillMaxWidth()) {
        for (day in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontWeight = FontWeight.Medium,
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).lowercase(),
            )
        }
    }
}

@Composable
private fun MonthlySelectorDay(
    day: CalendarDay,
    screenWidth: Dp,
    viewModel: TimetableState,
    noRipple: NoRippleInteractionSource,
    click: (LocalDate) -> Unit) {

    val colors = MaterialTheme.colors

    val inactive = day.position == DayPosition.OutDate || day.position == DayPosition.InDate
    val sunday = day.date.dayOfWeek.value == 7

    val today by remember { derivedStateOf { day.date == viewModel.currentDate.value } }
    val selected by remember { derivedStateOf { day.date == viewModel.selectedDayDate.value } }

    val colorAnimation by animateColorAsState(
        if (!selected) colors.background else SlightBonchBlue,
        tween(100, 0, LinearEasing)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    color = if (inactive || sunday) colors.secondaryVariant else if (selected) colors.surface else colors.onSurface,
                    fontWeight = if (inactive) FontWeight.Normal else FontWeight.Bold,
                )
            }
        if (!inactive) PairCountIndicatorRow(day.date, viewModel)
    }
}
