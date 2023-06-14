package voice_assistant

import java.util.Locale

object Utils {
    fun levenshtein(first: String, second: String): Double {
        var firstCopy = first
        var secondCopy = second
        firstCopy = firstCopy.lowercase(Locale.getDefault())
        secondCopy = secondCopy.lowercase(Locale.getDefault())
        val firstLength = firstCopy.length
        val secondLength = secondCopy.length

        val delta = Array(firstLength + 1) {
            IntArray(
                secondLength + 1
            )
        }
        for (i in 1..firstLength) {
            delta[i][0] = i
        }
        for (j in 1..secondLength) {
            delta[0][j] = j
        }
        for (j in 1..secondLength) {
            for (i in 1..firstLength) {
                if (firstCopy[i - 1] == secondCopy[j - 1]) {
                    delta[i][j] = delta[i - 1][j - 1]
                } else {
                    delta[i][j] = (delta[i - 1][j] + 1).coerceAtMost(
                        (delta[i][j - 1] + 1).coerceAtMost(delta[i - 1][j - 1] + 1)
                    )
                }
            }
        }
        return delta[firstLength][secondLength].toDouble()
    }
}