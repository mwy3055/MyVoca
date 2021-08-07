package hsk.practice.myvoca.data

import com.hsk.data.TodayWord
import hsk.practice.myvoca.room.todayword.RoomTodayWord

fun TodayWordImpl.toTodayWord(): TodayWord = TodayWord(todayId = id, wordId = wordId,checked=checked)

fun TodayWordImpl.toRoomTodayWord(): RoomTodayWord =
    RoomTodayWord(todayWordId = id, vocabularyId = wordId,checked=checked)

fun TodayWord.toTodayWordImpl(): TodayWordImpl = TodayWordImpl(id = todayId, wordId = wordId,checked=checked)