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
import com.s0und.sutapp.ui.timetableScreen.SelectedMonthTitle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthlySelector(viewModel: TimetableState, modifier: Modifier) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val noRipple = NoRippleInteractionSource()

    val firstDayOfWeek = remember { DayOfWeek.MONDAY }
    val daysOfWeek = daysOfWeek(firstDayOfWeek)

    val state = rememberCalendarState(
        startMonth = YearMonth.from(viewModel.startDate),
        endMonth = YearMonth.from(viewModel.endDate),
        firstVisibleMonth = YearMonth.from(viewModel.getfirstVisibleWeekDate()),
        firstDayOfWeek = firstDayOfWeek,
        outDateStyle = OutDateStyle.EndOfGrid
    )

    SelectedMonthTitle(state.firstVisibleMonth.yearMonth, modifier)

    HorizontalCalendar(
        modifier = modifier.requiredHeight(400.dp),
        calendarScrollPaged = true,
        state = state,
        dayContent = { day ->
            MonthlySelectorDay(day,
                today = day.date == viewModel.currentDate.value,
                inactive = day.position == DayPosition.OutDate || day.position == DayPosition.InDate,
                sunday = day.date.dayOfWeek.value == 7,
                screenWidth,
                viewModel,
                noRipple,
                modifier
            ) {
                viewModel.changeSelectedDay(it)
            }
        },
        monthHeader = { MonthHeader(daysOfWeek, modifier) }
    )
}

@Composable
private fun MonthHeader(
    daysOfWeek: List<DayOfWeek>,
    modifier: Modifier
) {
    Row(modifier.fillMaxWidth()) {
        for (day in daysOfWeek) {
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.secondaryVariant,
                fontWeight = FontWeight.Bold,
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
private fun MonthlySelectorDay(day: CalendarDay, today: Boolean, inactive: Boolean, sunday: Boolean, screenWidth: Dp, viewModel: TimetableState, noRipple: NoRippleInteractionSource, modifier: Modifier, click: (LocalDate) -> Unit) {

    val colors = MaterialTheme.colors

    val selected = day.date == viewModel.selectedDayDate.value

    val colorAnimation = animateColorAsState(
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
                        backgroundColor = colorAnimation.value,
                        disabledContentColor = colors.primary,
                        disabledBackgroundColor = colorAnimation.value
                    ),
                elevation = ButtonDefaults.elevation(0.dp),
                border = BorderStroke(4.dp, if (today) BonchBlue else Color.Transparent),
                shape = CircleShape,
                enabled = if (inactive) false else !selected, //&& !isSunday,
                interactionSource = noRipple,
                modifier = modifier
                    .size(58.dp)
                    .width(screenWidth / 9)
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (inactive || sunday) colors.secondaryVariant else if (selected) colors.surface else colors.onSurface,
                    fontWeight = if (inactive) FontWeight.Normal else FontWeight.ExtraBold,
                )
            }
        if (!inactive) PairCountIndicatorRow(day.date, viewModel, modifier)
    }
}