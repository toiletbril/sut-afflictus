package com.s0und.sutapp.ui.timetableScreen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kizitonwose.calendar.core.*
import com.s0und.sutapp.data.*
import com.s0und.sutapp.R
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.states.TimetableUIState
import com.s0und.sutapp.ui.theme.*
import com.s0und.sutapp.ui.timetableScreen.drawer.TimetableDrawer
import java.util.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TimetableScreen(
    viewModel: TimetableState,
    modifier: Modifier = Modifier
) {
    val swipeState = rememberSwipeRefreshState(viewModel.uiState.value == TimetableUIState.IsLoading)
    SwipeRefresh(
        swipeState,
        onRefresh = { viewModel.forceUpdate() },
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state,
                trigger,
                contentColor = SlightBonchBlue,
                scale = true,
                largeIndication = true
            ) },
    ) {
        Scaffold(
            scaffoldState = rememberScaffoldState(viewModel.drawerState.value),
            topBar = { TimetableTop(viewModel, modifier) },
            drawerContent = { TimetableDrawer(viewModel, modifier) },
            drawerGesturesEnabled = viewModel.drawerState.value.isOpen
        ) {

            val state = viewModel.uiState.value
            Column {
                when (state) {
                    TimetableUIState.NotLoaded -> {
                        viewModel.getDay()
                    }
                    TimetableUIState.IsLoading, TimetableUIState.ContentIsLoaded -> {
                        val selectedDay = viewModel.classesForDay.value
                        SubjectList(selectedDay.subjects, viewModel, modifier)
                    }
                    TimetableUIState.IsError -> {
                        ShowToast("${stringResource(R.string.ERROR)}:\n${stringResource(R.string.NO_INTERNET)}")
                        viewModel.getDay()
                    }
                }
            }
        } } }

@Composable
fun NoPairsToday(modifier: Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
        Column(modifier = modifier
            .fillMaxHeight()
            .padding(top = 180.dp, bottom = 200.dp)) {
            Text(
                text = stringResource(R.string.NOCLASSES),
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center
            )
            Spacer(modifier.weight(1f))
        } } } }

@Composable
fun ShowToast(text: String) {
    val text = text
    Toast.makeText(LocalContext.current, text, Toast.LENGTH_LONG).show()
}

//@Composable
//fun TimetableErrorText(errorMsg: String, modifier: Modifier) {
//    val text = "${stringResource(R.string.ERROR)}:\n${stringResource(R.string.NO_INTERNET)}"
//    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//        Row(modifier = modifier
//            .fillMaxHeight()
//            .padding(bottom = 200.dp), verticalAlignment = Alignment.CenterVertically) {
//            Text(
//                text = text,
//                fontWeight = FontWeight.ExtraBold,
//                style = MaterialTheme.typography.h6,
//                textAlign = TextAlign.Center
//            )
//      } }
//}
