package hsk.practice.myvoca

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


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