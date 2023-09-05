package me.mendez.ela.ui.screens.settings

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import me.mendez.ela.remote.ChatApi
import me.mendez.ela.settings.ElaSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<ElaSettings>,
) : ViewModel() {
    val state: Flow<ElaSettings>
        get() = dataStore.data


}