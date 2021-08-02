package hsk.practice.myvoca.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Preferences DataStore object. Delegated by preferenceDataStore().
 * Once this property is initialized, it can be accessed through the whole application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesDataStore @Inject constructor(@ApplicationContext val context: Context) {
    fun <T> getPreferencesFlow(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: default
        }

    suspend fun <T> getPreferences(key: Preferences.Key<T>, default: T): T =
        getPreferencesFlow(key, default).first()

    suspend fun <T> setPreferences(key: Preferences.Key<T>, value: T) {
        coroutineScope {
            launch {
                context.dataStore.edit { preferences ->
                    preferences[key] = value
                }
            }
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