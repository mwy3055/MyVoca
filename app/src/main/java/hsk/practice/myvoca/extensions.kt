package hsk.practice.myvoca

fun String?.containsOnlyAlphabet(): Boolean {
    if (this.isNullOrEmpty()) return false
    this.forEach {
        if (it !in 'a'..'z' && it !in 'A'..'Z' && it != '%') {
            return false
        }
    }
    return true
}