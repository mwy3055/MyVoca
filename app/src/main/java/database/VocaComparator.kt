package database

import java.util.*

/**
 * Classes for comparing vocabulary.
 * Implemented as Singleton to save resources.
 *
 * Supports comparing with eng, add_time
 * Used in SeeAllFragment to sort the RecyclerView with specific criteria.
 */
object VocaComparator {
    private var addedTimeComparator: AddedTimeComparator? = null
    private var engComparator: EngComparator? = null
    fun getAddedTimeComparator(): AddedTimeComparator? {
        if (addedTimeComparator == null) {
            synchronized(AddedTimeComparator::class.java) { addedTimeComparator = AddedTimeComparator() }
        }
        return addedTimeComparator
    }

    fun getEngComparator(): EngComparator? {
        if (engComparator == null) {
            synchronized(EngComparator::class.java) { engComparator = EngComparator() }
        }
        return engComparator
    }

    private class AddedTimeComparator : Comparator<Vocabulary?> {
        // last added first
        override fun compare(o1: Vocabulary?, o2: Vocabulary?): Int {
            return o2.addedTime - o1.addedTime
        }

        override fun equals(obj: Any?): Boolean {
            return if (obj is AddedTimeComparator) {
                this === obj as AddedTimeComparator?
            } else false
        }
    }

    private class EngComparator : Comparator<Vocabulary?> {
        override fun compare(o1: Vocabulary?, o2: Vocabulary?): Int {
            return o1.eng.compareTo(o2.eng)
        }

        override fun equals(obj: Any?): Boolean {
            return if (obj is EngComparator) {
                this === obj as EngComparator?
            } else false
        }
    }
}