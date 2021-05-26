package hsk.practice.myvoca.ui.quiz

import hsk.practice.myvoca.framework.RoomVocabulary

data class Quiz(
    val quizList: List<RoomVocabulary>,
    val answerIndex: Int
) {
    val answer: RoomVocabulary
        get() = quizList[answerIndex]
}