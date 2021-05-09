package hsk.practice.myvoca

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Preferences DataStore object. Delegated by preferenceDataStore().
 * Once this property is initialized, it can be accessed through the whole application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Checks if the given string contains only alphabet.
 *
 * @return `true` if string contains only alphabet, `false` otherwise
 */
fun String?.containsOnlyAlphabet(): Boolean {
    if (this.isNullOrEmpty()) return false
    this.forEach {
        if (it !in 'a'..'z' && it !in 'A'..'Z' && it != '%') {
            return false
        }
    }
    return true
}


@Throws(Exception::class)
fun <T> LiveData<T>.getValueBlocking(): T {
    var value: T? = null
    val latch = CountDownLatch(1)
    val innerObserver = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(innerObserver)
    latch.await(10, TimeUnit.SECONDS)
    return value ?: throw IllegalStateException("Value not set")
}