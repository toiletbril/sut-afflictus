package com.s0und.sutapp.states

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s0und.sutapp.data.SettingsManager
import com.s0und.sutapp.parser.UniGroup
import com.s0und.sutapp.parser.bgetGroups
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class InitUIState {
    object NotLoaded: InitUIState()
    object IsLoading: InitUIState()
    object ContentIsLoaded: InitUIState()
    object GroupPicked: InitUIState()
    object IsError: InitUIState()
}

class InitState(private val settingsManager: SettingsManager): ViewModel() {

    private val _uiState = mutableStateOf<InitUIState>(InitUIState.NotLoaded)
    val uiState: State<InitUIState>
        get() = _uiState

    private var _groups = MutableStateFlow(emptyList<UniGroup>())
    val groups = _groups.asStateFlow()

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String>
        get() = _errorMessage

    private val _searchText = mutableStateOf("")
    val searchText: State<String>
        get() = _searchText

    private val _selectedGroupId = mutableStateOf("")
    val selectedGroupId: State<String>
        get() = _selectedGroupId

    private val _selectedGroupName = mutableStateOf("")
    val selectedGroupName: State<String>
        get() = _selectedGroupName

    private val _groupFound = mutableStateOf(false)
    val groupFound: State<Boolean>
        get() = _groupFound

    fun isGroupFound(found: Boolean, ID: String? = null, name: String? = null) {
        _groupFound.value = found
        if (found) {
            _selectedGroupId.value = ID!!
            _selectedGroupName.value = name!!
        } else {
            _selectedGroupId.value = ""
            _selectedGroupName.value = ""
        }
    }

    fun changeState(state: InitUIState) {
        _uiState.value = state
    }

    fun groupFound() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsManager.storeGroupData(selectedGroupId.value, selectedGroupName.value) {
                _uiState.value = InitUIState.GroupPicked
            }
        }
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun requestGroups() {
        val groupRequester = viewModelScope.launch(Dispatchers.IO) {
            bgetGroups() {
                if (it.isSuccess) {
                    val value = it.getOrNull()!!
                    _groups.value = value
                    _uiState.value = InitUIState.ContentIsLoaded
                } else {
                    _errorMessage.value = it.exceptionOrNull()!!.toString()
                    _uiState.value = InitUIState.IsError
                }
            }
        }
    }

    fun getGroups() {
        _uiState.value = InitUIState.IsLoading

        viewModelScope.launch(Dispatchers.IO) {
            if (settingsManager.wasInitialized.first() == "YES") {
                _uiState.value = InitUIState.GroupPicked
            } else requestGroups()
        }
    }
}