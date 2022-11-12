package com.s0und.sutapp.ui.timetableScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R
import com.s0und.sutapp.data.getFormattedWeek
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.timetableScreen.selector.MonthlySelector
import com.s0und.sutapp.ui.timetableScreen.selector.WeeklySelector
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun TimetableTop(viewModel: TimetableState, modifier: Modifier) {

    val group = viewModel.selectedGroupName.value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(bottom = 16.dp)
            .animateContentSize(tween(150, 0, FastOutSlowInEasing))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -20f) viewModel.changeSelector(false)
                    if (dragAmount > 20f) viewModel.changeSelector(true)
                }
            }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = group,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Black,
                modifier = modifier
                    .offset(32.dp, 8.dp)
            )
            Spacer(modifier.weight(1f))
            TimetableSettingsIconButton(viewModel, modifier)
        }
        SelectedMonthTitle(viewModel.selectedDate.value)

        when (viewModel.selectorState.value) {
            true -> MonthlySelector(viewModel)
            false -> WeeklySelector(viewModel)
        }

        SelectedWeekTitle(viewModel, viewModel.startDate)
    } }

@Composable
fun TimetableSettingsIconButton(viewModel: TimetableState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    IconButton(modifier = modifier
        .padding(end = 32.dp, top = 16.dp)
        .size(24.dp),
        onClick = { scope.launch {
            viewModel.drawerState.value.open()
        } }
    ) {
        Icon(painter = painterResource(R.drawable.settingsicon), contentDescription = "refresh")
    }
}

@Composable
fun PairCountIndicatorRow(day: LocalDate, viewModel: TimetableState) {

    Row(
        modifier = Modifier
            .offset(y = (-16).dp)
            .requiredHeight(6.dp)
            .requiredWidthIn(6.dp, 30.dp)
    ) {

        var colors by remember { mutableStateOf(listOf<Color>()) }

        LaunchedEffect(viewModel.mapOfDays.value) {
            viewModel.getPairColors(day) {
                colors = it
            }
        }

        if (colors.isNotEmpty())
            for (i in colors) {
                PairCountIndicator(i)
            }

    }
}

@Composable
fun PairCountIndicator(color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(6.dp)
            .background(color)

    )
}

@Composable
fun SelectedMonthTitle(month: YearMonth) {
    val selectedMonth = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = selectedMonth.lowercase(),
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun SelectedWeekTitle(viewModel: TimetableState, startDate: LocalDate) {
    val dayDate = viewModel.selectedDayDate.value
    val weekNumber = (dayDate.dayOfYear + 7 - (startDate.dayOfYear - startDate.dayOfWeek.value + 1)).floorDiv(7)
    val weekDaysTitle = getFormattedWeek(dayDate)
    Text(
        modifier = Modifier.padding(bottom = 4.dp, top = 0.dp),
        text = "$weekNumber${stringResource(R.string.TH)} ${stringResource(R.string.WEEK)} ${stringResource(R.string.FROM)} $weekDaysTitle",
        style = MaterialTheme.typography.body2,
        fontWeight = FontWeight.Medium
    )
}
