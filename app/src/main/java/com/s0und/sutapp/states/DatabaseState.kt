package com.s0und.sutapp.states

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.s0und.sutapp.data.AppDatabase
import com.s0und.sutapp.data.DayRepository
import com.s0und.sutapp.data.UniDay
import com.s0und.sutapp.parser.UniSubject

class DatabaseState(application: Application): AndroidViewModel(application) {
    private val repository: DayRepository

    init {
        val dayDao = AppDatabase.getDatabase(application.applicationContext).uniDayDao()
        repository = DayRepository(dayDao)
    }

    suspend fun addDay(week: UniDay) {
        repository.addDay(week)
    }

    suspend fun readAllData(group: String): Map<String, List<UniSubject>> {
        val listOfDays = repository.readAllData("%$group%")
        val mapOfDays = mutableMapOf<String, List<UniSubject>>()

        for (i in listOfDays) {
            mapOfDays[i.id] = i.subjects
        }
        return mapOfDays
    }

    fun readDay(Id: String): UniDay? {
        return repository.readDay(Id)
    }
}
