package com.s0und.sutapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.s0und.sutapp.data.SettingsManager
import com.s0und.sutapp.states.DatabaseState
import com.s0und.sutapp.states.InitState
import com.s0und.sutapp.states.TimetableState
import com.s0und.sutapp.ui.*
import com.s0und.sutapp.ui.theme.*
import com.s0und.sutapp.ui.timetableScreen.TimetableScreen
import kotlinx.coroutines.flow.first

/***
TODO:
 1. Optimize month view more
 2. Navigation using destinations
 other:
 teacher's timetable?
***/

class MainActivity: ComponentActivity() {

    private lateinit var databaseState: DatabaseState
    private lateinit var settingsManager: SettingsManager
    private lateinit var timetableState: TimetableState
    private lateinit var _broadcastReceiver: BroadcastReceiver

    override fun onStart() {
        settingsManager = SettingsManager(application)
        databaseState = DatabaseState(application)
        timetableState = TimetableState(databaseState, settingsManager)

        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    timetableState.updateTime()
                    timetableState.updateDate()
                }
            }
        }
        registerReceiver(_broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

        super.onStart()
    }

    override fun onResume() {
        timetableState.updateTime()
        timetableState.updateDate()

        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            AbobaTheme {

                rememberSystemUiController()
                    .setSystemBarsColor(MaterialTheme.colors.background)

                var initialized by remember { mutableStateOf(-1)}

                LaunchedEffect(timetableState.launched.value) {
                    if (settingsManager.wasInitialized.first() == "YES") {

                        val groupID = settingsManager.groupIDFlow.first()
                        val groupName = settingsManager.groupNameFlow.first()

                        timetableState.convertDatabaseToMap()
                        timetableState.changeGroup(groupID, groupName)

                        initialized = 1
                    } else
                        initialized = 0
                }

                when (initialized) {
                    -1 -> InitLoading()
                    0 -> InitScreen(InitState(settingsManager), timetableState)
                    1 -> TimetableScreen(timetableState)
                }
            }
        }
    }

    @Suppress("Senseless_Comparison")
    override fun onStop() {
        if (_broadcastReceiver != null) unregisterReceiver(_broadcastReceiver)
        super.onStop()
    }
}
