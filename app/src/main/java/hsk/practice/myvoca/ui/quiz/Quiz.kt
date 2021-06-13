package hsk.practice.myvoca.ui.quiz

import hsk.practice.myvoca.VocabularyImpl

data class Quiz(
    val quizList: List<VocabularyImpl>,
    val answerIndex: Int
) {
    val answer: VocabularyImpl
        get() = quizList[answerIndex]
}