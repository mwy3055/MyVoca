package hsk.practice.myvoca

/**
 * Holder class for Singleton. This holder can hold a class which has one parameter in its constructor.
 * <b>
 * @param T Class which will be treated as singleton
 * @param A A single parameter of the constructor of T
 */
open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}