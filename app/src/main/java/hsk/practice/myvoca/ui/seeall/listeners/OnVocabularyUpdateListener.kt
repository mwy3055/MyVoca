package hsk.practice.myvoca.ui.seeall.listeners

/**
 * Custom listener which defines a behavior when an item is edited (mainly in VocaRecyclerView).
 * Implemented at SeeAllFragment.editVocabulary().
 */
interface OnVocabularyUpdateListener {
    fun updateVocabulary(position: Int)
}