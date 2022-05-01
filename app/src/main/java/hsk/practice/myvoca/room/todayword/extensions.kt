package hsk.practice.myvoca.room.todayword

import com.hsk.data.TodayWord

fun RoomTodayWord.toTodayWord() =
    TodayWord(todayId = todayWordId, wordId = vocabularyId, checked = checked)

fun TodayWord.toRoomTodayWord() =
    RoomTodayWord(todayWordId = todayId, vocabularyId = wordId, checked = checked)