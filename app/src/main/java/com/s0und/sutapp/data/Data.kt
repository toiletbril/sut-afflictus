package com.s0und.sutapp.data

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.room.*
import com.google.gson.Gson
import com.s0und.sutapp.parser.UniSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

@Entity(tableName = "uni_days")
data class UniDay(
    @PrimaryKey
    val id: String,
    val subjects: List<UniSubject>
)

@Dao
interface UniDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDay(day: UniDay)

    @Query(value = "SELECT * FROM uni_days WHERE id LIKE :group")
    fun readAllData(group: String): List<UniDay>

    @Query(value = "SELECT * FROM uni_days WHERE id = :ID")
    fun readDay(ID: String): UniDay
}

class Converters {
    @TypeConverter
    fun listToJsonString(value: List<UniSubject>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String) = Gson().fromJson(value, Array<UniSubject>::class.java).toList()
}
