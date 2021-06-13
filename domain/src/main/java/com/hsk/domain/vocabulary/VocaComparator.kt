package com.hsk.domain.vocabulary

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
            synchronized(AddedTimeComparator::class.java) {
                addedTimeComparator = AddedTimeComparator()
            }
        }
        return addedTimeComparator
    }

    fun getEngComparator(): EngComparator? {
        if (engComparator == null) {
            synchronized(EngComparator::class.java) { engComparator = EngComparator() }
        }
        return engComparator
    }

    class IdComparator : Comparator<Vocabulary?> {
        override fun compare(o1: Vocabulary?, o2: Vocabulary?): Int {
            return if (o1 != null && o2 != null) {
                o1.id.compareTo(o2.id)
            } else 1
        }

        override fun equals(other: Any?): Boolean {
            return if (other is IdComparator) {
                this === other
            } else false
        }
    }

    class AddedTimeComparator : Comparator<Vocabulary?> {
        // last added first
        override fun compare(o1: Vocabulary?, o2: Vocabulary?): Int {
            return if (o1 != null && o2 != null) {
                if (o2.addedTime - o1.addedTime != 0L) 1 else 0
            } else 1
        }

        override fun equals(other: Any?): Boolean {
            return if (other is AddedTimeComparator) {
                this === other as AddedTimeComparator?
            } else false
        }
    }

    class EngComparator : Comparator<Vocabulary?> {
        override fun compare(o1: Vocabulary?, o2: Vocabulary?): Int {
            return if (o1 != null && o2 != null) {
                o1.eng.compareTo(o2.eng)
            } else 1
        }

        override fun equals(other: Any?): Boolean {
            return if (other is EngComparator) {
                this === other as EngComparator?
            } else false
        }
    }
}