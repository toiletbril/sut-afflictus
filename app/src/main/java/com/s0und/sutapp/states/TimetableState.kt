package com.s0und.sutapp.states

import android.content.Context
import androidx.compose.material.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.atStartOfMonth
import com.s0und.sutapp.data.*
import com.s0und.sutapp.parser.*
import com.s0und.sutapp.ui.theme.BonchOrange
import com.s0und.sutapp.ui.theme.BonchRed
import com.s0und.sutapp.ui.theme.BonchYellow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth


sealed class TimetableUIState {
    object NotLoaded: TimetableUIState()
    object IsLoading: TimetableUIState()
    object ContentIsLoaded: TimetableUIState()
    object IsError: TimetableUIState()
}

class TimetableState(private val databaseState: DatabaseState, private val settingsManager: SettingsManager, context: Context): ViewModel() {
    private val _uiState = mutableStateOf<TimetableUIState>(TimetableUIState.NotLoaded)
    val uiState: State<TimetableUIState>
        get() = _uiState

    val drawerState = mutableStateOf(DrawerState(DrawerValue.Closed))

    val loggedIn = mutableStateOf(false)

    private val _uiTheme = mutableStateOf(0)
    val uiTheme: State<Int>
        get() = _uiTheme

    private var _classesForDay = mutableStateOf(UniDay("", emptyList()))
    val classesForDay: State<UniDay>
        get() = _classesForDay

    private var _mapOfDays = mutableStateOf(mapOf<String, List<UniSubject>>())
    val mapOfDays: State<Map<String, List<UniSubject>>>
        get() = _mapOfDays

    private var _group1 = mutableStateOf(listOf("", ""))
    val group1 : State<List<String>>
        get() = _group1

    private var _group2 = mutableStateOf(listOf("", ""))
    val group2 : State<List<String>>
        get() = _group2

    private var _group3 = mutableStateOf(listOf("", ""))
    val group3 : State<List<String>>
        get() = _group3

    private val sharedPreferences = bgetEncryptedSharedPreferences(context)

    fun logIn(login: String, password: String, callback: (Result<Unit>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val check = checkCredentials(login, password)

            if (check.isSuccess)
                if (check.getOrNull()!!) {
                    sharedPreferences.edit()
                        .putString("login", login)
                        .putString("pswd", password)
                        .apply()
                    callback.invoke(Result.success(Unit))
                } else
                    callback.invoke(Result.failure(Throwable("WRONG_PASSWORD")))

            else callback.invoke(Result.failure(check.exceptionOrNull()!!))
        }
    }

    fun logOut() {
        if (loggedIn.value)
            viewModelScope.launch(Dispatchers.IO) {
                sharedPreferences.edit()
                    .clear()
                    .apply()
                loggedIn.value = false
            }
    }

    fun getSharedPreferences(callback: (Pair<String, String>) -> (Unit)) {
        viewModelScope.launch(Dispatchers.IO) {
            val one = sharedPreferences.getString("login", "0")
            val two = sharedPreferences.getString("pswd", "0")

            callback.invoke(Pair(one!!, two!!))
        }
    }

    fun getAccount(callback: (Result<AccountData>) -> (Unit)) {
        getSharedPreferences {
            viewModelScope.launch(Dispatchers.IO) {
                callback.invoke(getAccountData(it.first, it.second))
            }
        }
    }

    fun checkIn(callback: (Result<Unit>) -> (Unit)) {
        getSharedPreferences {
            viewModelScope.launch(Dispatchers.IO) {
                callback.invoke(checkInCurrentPair(it.first, it.second))
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = mutableStateOf(ModalBottomSheetValue.Hidden)

    suspend fun convertDatabaseToMap() {
        _mapOfDays.value = databaseState.readAllData(selectedGroupID.value)
    }

    fun convertDatabaseToMapCompose() {
        viewModelScope.launch(Dispatchers.IO) {
            _mapOfDays.value = databaseState.readAllData(selectedGroupID.value)
        }
    }

    private var _currentDate = mutableStateOf(LocalDate.now())
    val currentDate: State<LocalDate>
        get() = _currentDate

    fun updateDate() {
        _currentDate.value = LocalDate.now()
    }

    private val _currentTime = mutableStateOf(LocalTime.now())
    val currentTime: State<LocalTime>
        get() = _currentTime

    fun updateTime() {
        _currentTime.value = LocalTime.now()
    }

    private val _selectorState = mutableStateOf(false)
    val selectorState: State<Boolean>
        get() = _selectorState

    fun changeSelector(selector: Boolean) {
        _selectorState.value = selector
    }

    private val semester = if (currentDate.value.month.value in 9..12) 1 else 2
    private val currentYearMonth: YearMonth = YearMonth.now()

    val startDate: LocalDate = if (semester == 1) YearMonth.of(currentYearMonth.year, 9).atStartOfMonth() else YearMonth.of(currentYearMonth.year, 1).atStartOfMonth()
    val endDate: LocalDate = if (semester == 1) YearMonth.of(currentYearMonth.year, 12).atEndOfMonth() else YearMonth.of(currentYearMonth.year, 5).atEndOfMonth()

    private val _selectedDayDate = mutableStateOf(LocalDate.now())
    val selectedDayDate: State<LocalDate>
        get() = _selectedDayDate

    fun getfirstVisibleWeekDate(): LocalDate {
        return selectedDayDate.value
    }

    fun changeSelectedDay(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedDayDate.value = date
            getDay()
        }
    }

    val launched = mutableStateOf(false)

    fun loadTimetable() {
        viewModelScope.launch(Dispatchers.IO) {

            _selectedGroupID.value = settingsManager.groupIDFlow.first()
            _selectedGroupName.value = settingsManager.groupNameFlow.first()
            launched.value = true

            forceUpdate()
        }
    }

    private val _selectedGroupID = mutableStateOf("")
    val selectedGroupID: State<String>
        get() = _selectedGroupID

    private val _selectedGroupName = mutableStateOf("")
    val selectedGroupName: State<String>
        get() = _selectedGroupName

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String>
        get() = _errorMessage

    fun getPairColors(day: LocalDate = selectedDayDate.value, callback: (List<Color>) -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {

                val id = "${selectedGroupID.value}$day"
                val aday = mapOfDays.value[id]

                if (aday != null) {
                    val colors = mutableListOf<Color>()
                    for (it in aday) {

                        if (it.subjectName.endsWith("(2)")) continue
                        if (it.subjectName.endsWith("(3)")) continue

                        when (it.classType) {
                            "пр" -> colors.add(BonchOrange)
                            "лек" -> colors.add(BonchYellow)
                            "лаб" -> colors.add(BonchRed)
                        }
                    }
                    callback.invoke(colors)
                }
            }
        }

    fun changeGroup(groupID: String, groupName: String) {
        _selectedGroupID.value = groupID
        _selectedGroupName.value = groupName
    }

    fun forceUpdate(groupID: String = selectedGroupID.value) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.value != TimetableUIState.IsLoading) {
                _uiState.value = TimetableUIState.IsLoading

                val classes = getClassesForSemester(groupID, databaseState)

                if (classes.isSuccess) {

                    convertDatabaseToMap()
                    getDay()

                } else {
                        _errorMessage.value = classes.exceptionOrNull()!!.toString()
                        _uiState.value = TimetableUIState.IsError
                    }
                }
            }
        updateDate()
        updateTime()
        }


    fun getDay(groupID: String = selectedGroupID.value, day: LocalDate = selectedDayDate.value) {

        viewModelScope.launch(Dispatchers.IO) {

            if (uiState.value == TimetableUIState.NotLoaded) _uiState.value = TimetableUIState.IsLoading

            val dayID = "$groupID$day"

            val readDay = mapOfDays.value[dayID]

            if (readDay != null) {
                println("founds !!!!")
                _classesForDay.value = UniDay(dayID, readDay)
            }
            else {
                println("emty !!!!")
                val emptyDay = UniDay(dayID, emptyList())
                _classesForDay.value = emptyDay
            }
            _uiState.value = TimetableUIState.ContentIsLoaded
        }
    }
}
