package hsk.practice.myvoca.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class ExtensionsTest {

    @Test
    fun testArrayRemoved_Exists() {
        val array = Array(10) { it }

        val expected = Array(9) { it }.toList()
        val actual = array.removed(9)
        assertEquals(expected, actual)
    }

    @Test
    fun testArrayRemoved_NotExists() {
        val array = Array(10) { it }

        val expected = array.copyOf().toList()
        val actual = array.removed(11)
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionXor_RemoveElement() {
        val numbers = (1..10).toList()

        val expected = (1..9).toList()
        val actual = numbers.xor(10).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionXor_AddElement() {
        val numbers = (1..9).toList()

        val expected = (1..10).toList()
        val actual = numbers.xor(10).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionRandoms_ManyElements() {
        val numbers = (1..100).toList()

        val randomSize = 10
        val randoms = numbers.randoms(randomSize)
        assertEquals(randomSize, randoms.size)
        randoms.forEach { random -> assert(numbers.contains(random)) }
    }

    @Test
    fun testCollectionRandoms_FewElements() {
        val numbers = (1..5).toList()

        val randomSize = 10
        val randoms = numbers.randoms(randomSize)
        assertEquals(numbers, randoms)
    }

    @Test
    fun distinctRandoms_SameElements() {
        val manyOnes = (1..10).map { 1 }

        val actual = manyOnes.distinctRandoms(4)
        val expected = listOf(1)
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionTruncate_SliceMiddle() {
        val numbers = (1..100).toMutableList()

        val truncateSize = 50
        val expected = (1..50).toList()
        val actual = numbers.truncate(truncateSize)
        numbers[11] = 1
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionTruncate_SliceOver() {
        val numbers = (1..100).toMutableList()

        val truncateSize = 1000
        val expected = (1..100).toList()
        val actual = numbers.truncate(truncateSize)
        numbers[11] = 1
        assertEquals(expected, actual)
    }

    @Test
    fun testCollectionTruncate_EmptyArray() {
        val emptyList = emptyList<Int>()

        val truncateSize = 10
        val actual = emptyList.truncate(truncateSize)
        assertEquals(emptyList, actual)
    }

    @Test
    fun testCollectionTruncate_NegativeSize() {
        var exceptionOccur = false

        val numbers = (1..10).toList()

        val truncateSize = -10
        try {
            numbers.truncate(truncateSize)
        } catch (e: IllegalArgumentException) {
            assertEquals("Size must be non-negative", e.message)
            exceptionOccur = true
        } finally {
            assert(exceptionOccur)
        }
    }

    @Test
    fun testGcd_WithOne() {
        testGcd(1, 10, 1)
    }

    @Test
    fun testGcd_SameNumber() {
        testGcd(20, 20, 20)
    }

    @Test
    fun testGcd_PrimeNumbers() {
        testGcd(3, 7, 1)
    }

    @Test
    fun testGcd_NormalCase() {
        testGcd(25, 95, 5)
    }

    private fun testGcd(num1: Int, num2: Int, expected: Int) {
        val actual = num1.gcd(num2)
        assertEquals(expected, actual)
    }

    @Test
    fun testSecondsLeft_Noon() {
        val noon = LocalTime.NOON
        val expected = (60 * 60 * 24L) / 2
        testSecondsLeft(noon, expected)
    }

    @Test
    fun testSecondsLeft_Midnight() {
        val midnight = LocalTime.MIDNIGHT
        val expected = 60 * 60 * 24L
        testSecondsLeft(midnight, expected)
    }

    private fun testSecondsLeft(current: LocalTime, expected: Long) {
        val actual = getSecondsLeftOfDay(current)
        assertEquals(expected, actual)
    }

    @Test
    fun testTimeDiffString_Min() {
        val minEpoch = LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC)
        testTimeDiffString(100L, minEpoch, "오래 전")
    }

    @Test
    fun testTimeDiffString_Second() {
        testTimeDiffString(1000L, 1001L, "1초 전")
    }

    @Test
    fun testTimeDiffString_Minute() {
        testTimeDiffString(1000L, 1120L, "2분 전")
    }

    @Test
    fun testTimeDiffString_Hour() {
        val hourToSecond = 60 * 60L
        val coef = 10
        testTimeDiffString(2000L, 2000L + (hourToSecond * coef), "${coef}시간 전")
    }

    @Test
    fun testTimeDiffString_Day() {
        val dayToSecond = 60 * 60 * 24L
        val coef = 7
        testTimeDiffString(100L, 100L + dayToSecond * coef, "${coef}일 전")
    }

    private fun testTimeDiffString(currentEpoch: Long, anotherEpoch: Long, expected: String) {
        val currentTime = getEpochSecond(currentEpoch)
        val anotherTime = getEpochSecond(anotherEpoch)

        val actual = getTimeDiffString(currentTime, anotherTime)
        assertEquals(expected, actual)
    }

    private fun getEpochSecond(epochSecond: Long) =
        LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC).toEpochSecond(
            ZoneOffset.UTC
        )
}