package hsk.practice.myvoca

import java.io.Serializable

data class VocabularyImpl(
    val id: Int = 0,
    val eng: String,
    val kor: String?,
    val addedTime: Long,
    val lastEditedTime: Long,
    val memo: String?
) : Serializable {

    val answerString: String
        get() = "$eng: ${kor?.replace("\n", " ")}"

    companion object {
        val nullVocabulary: VocabularyImpl
            get() = VocabularyImpl(
                id = 0,
                eng = "null",
                kor = "널",
                addedTime = System.currentTimeMillis(),
                lastEditedTime = System.currentTimeMillis(),
                memo = ""
            )
    }
}

