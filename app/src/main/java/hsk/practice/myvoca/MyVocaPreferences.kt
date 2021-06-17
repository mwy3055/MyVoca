package hsk.practice.myvoca

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Preferences DataStore object. Delegated by preferenceDataStore().
 * Once this property is initialized, it can be accessed through the whole application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun <T> Context.getPreferencesFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
    dataStore.data.map { preferences ->
        preferences[key] ?: defaultValue
    }

suspend fun <T> Context.getPreferences(key: Preferences.Key<T>, defaultValue: T): T {
    return getPreferencesFlow(key, defaultValue).first()
}

suspend fun <T> Context.setPreferenceValue(key: Preferences.Key<T>, value: T) =
    coroutineScope {
        launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }


/**
 * Stores preference keys only.
 */
object MyVocaPreferences {
    private const val quizCorrectKeyString = "quiz_correct"
    val quizCorrectKey = intPreferencesKey(quizCorrectKeyString)

    private const val quizWrongKeyString = "quiz_wrong"
    val quizWrongKey = intPreferencesKey(quizWrongKeyString)
}