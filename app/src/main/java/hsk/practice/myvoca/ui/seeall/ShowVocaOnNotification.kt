package hsk.practice.myvoca.ui.seeall

import hsk.practice.myvoca.framework.RoomVocabulary


interface ShowVocaOnNotification {
    /**
     * Show a vocabulary at the notification.
     * @param target vocabulary to show
     */
    fun showVocabularyOnNotification(target: RoomVocabulary)
}