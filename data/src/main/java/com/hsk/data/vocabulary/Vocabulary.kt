package com.hsk.data.vocabulary

import java.io.Serializable

data class Vocabulary(
    val id: Int,
    val eng: String,
    val kor: String?,
    val addedTime: Long,
    val lastEditedTime: Long,
    val memo: String?
) : Serializable

val nullVocabulary: Vocabulary
    get() = Vocabulary(
        id = 0,
        eng = "null",
        kor = "널",
        addedTime = System.currentTimeMillis(),
        lastEditedTime = System.currentTimeMillis(),
        memo = ""
    )
