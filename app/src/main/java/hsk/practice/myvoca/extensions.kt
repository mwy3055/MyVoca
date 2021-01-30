package hsk.practice.myvoca


/**
 * Checks if the given string contains only alphabet
 *
 * @param str string to check
 * @return true if string contains only alphabet, false otherwise
 */
fun String?.containsOnlyAlphabet(): Boolean {
    if (this.isNullOrEmpty()) return false
    this.forEach {
        if (it !in 'a'..'z' && it !in 'A'..'Z' && it != '%') {
            return false
        }
    }
    return true
}