package com.s0und.sutapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.s0und.sutapp.*

@TypeConverters(Converters::class)
@Database(entities = [UniDay::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun uniDayDao(): UniDayDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val _instance = INSTANCE
            if (_instance != null) {
                return INSTANCE!!
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "uni_weeks"
                ).build()
                INSTANCE = instance
                return instance
            }
        } } }

class DayRepository(private val uniDayDao: UniDayDao) {

    suspend fun readAllData(group: String): List<UniDay> {
        return uniDayDao.readAllData(group)
    }

    suspend fun addDay(day: UniDay) {
        uniDayDao.addDay(day)
    }

    fun readDay(Id: String): UniDay {
        return uniDayDao.readDay(Id)
    }

}
