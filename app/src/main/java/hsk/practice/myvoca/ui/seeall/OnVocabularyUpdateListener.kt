package hsk.practice.myvoca.ui.seeall

import hsk.practice.myvoca.framework.RoomVocabulary

/**
 * Custom listener which defines a behavior when an item is edited (mainly in VocaRecyclerView).
 * Implemented at SeeAllFragment.editVocabulary().
 */
interface OnVocabularyUpdateListener {
    fun editVocabulary(position: Int, vocabulary: RoomVocabulary?)
}