package com.s0und.sutapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(context: Context) {

    companion object {
        val GROUP_SELECTED_ID_KEY = stringPreferencesKey("SELECTED_GROUP_ID")
        val GROUP_SELECTED_NAME_KEY = stringPreferencesKey("SELECTED_GROUP_NAME")
        val INITIALIZATION_COMPLETE = stringPreferencesKey("INITIALIZATION_COMPLETE")

        val GROUP1 = stringSetPreferencesKey("GROUP1")
        val GROUP2 = stringSetPreferencesKey("GROUP1")
        val GROUP3 = stringSetPreferencesKey("GROUP1")

        @Volatile
        private var INSTANCE: DataStore<Preferences>? = null
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

        fun getDatastore(context: Context): DataStore<Preferences> {
            val _instance = INSTANCE
            if (_instance != null) { return INSTANCE!! }
            synchronized(this) {
                val instance = context.dataStore
                INSTANCE = instance
                return instance
            }
        }
    }

    private val mDataStore = getDatastore(context)

    suspend fun storeGroupData(ID: String, name: String, callback: () -> Unit) {
            mDataStore.edit { settings ->
                settings[GROUP_SELECTED_ID_KEY] = ID
                settings[GROUP_SELECTED_NAME_KEY] = name
                settings[INITIALIZATION_COMPLETE] = "YES"
            }
        storeExtraGroup(ID, name, 1) {
            callback.invoke()
        }
    }

    suspend fun storeExtraGroup(ID: String, name: String, n: Int, callback: () -> Unit) {
        val groupData = setOf(ID, name)
        mDataStore.edit { settings ->
            when (n) {
                1 -> settings[GROUP1] = groupData
                2 -> settings[GROUP2] = groupData
                3 -> settings[GROUP3] = groupData
            }
        }
        callback.invoke()
    }

    suspend fun changeSelectedGroupKeys(ID: String, name: String, callback: () -> Unit) {
        mDataStore.edit { settings ->
            settings[GROUP_SELECTED_ID_KEY] = ID
            settings[GROUP_SELECTED_NAME_KEY] = name
        }
        callback.invoke()
    }

    val groupIDFlow: Flow<String> = mDataStore.data.map {
        it[GROUP_SELECTED_ID_KEY] ?: ""
    }

    val groupNameFlow: Flow<String> = mDataStore.data.map {
        it[GROUP_SELECTED_NAME_KEY] ?: ""
    }

    val wasInitialized = mDataStore.data.map {
        it[INITIALIZATION_COMPLETE] ?: ""
    }

    val groupList = mDataStore.data.map {
        it[GROUP1] ?: ""
    }


}

