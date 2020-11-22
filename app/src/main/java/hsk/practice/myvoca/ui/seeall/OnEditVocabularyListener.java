package hsk.practice.myvoca.ui.seeall;

import database.Vocabulary;

/**
 * Custom listener which defines a behavior when an item is edited (mainly in VocaRecyclerView).
 * Implemented at SeeAllFragment.editVocabulary().
 */
public interface OnEditVocabularyListener {

    void editVocabulary(int position, Vocabulary vocabulary);

}
