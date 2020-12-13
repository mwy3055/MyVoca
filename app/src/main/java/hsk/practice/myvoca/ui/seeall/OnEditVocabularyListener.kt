package hsk.practice.myvoca.ui.seeall

import database.Vocabulary

/**
 * Custom listener which defines a behavior when an item is edited (mainly in VocaRecyclerView).
 * Implemented at SeeAllFragment.editVocabulary().
 */
interface OnEditVocabularyListener {
    fun editVocabulary(position: Int, vocabulary: Vocabulary?)
}