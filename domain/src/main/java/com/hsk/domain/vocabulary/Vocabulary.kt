package com.hsk.domain.vocabulary

import java.io.Serializable

data class Vocabulary(val eng: String, val kor: String?, val addedTime: Long, val lastEditedTime: Long, val memo: String?) : Serializable
