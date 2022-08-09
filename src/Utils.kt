import java.nio.file.Paths
import kotlin.math.atan2
import kotlin.math.sqrt

object Utils {
    fun readFile(fileName: String, folder: String = "data"): List<String> {
        return Paths.get(folder, fileName).toFile().readLines()
    }

    private val validZeroPoints = setOf("N", "S", "E", "W", "NE", "SE", "NW", "SW")
    /**
     * Returns radians from a given set point in [0, 2*PI). By default, Counter-clockwise is considered positive and
     * East is the zero point
     */
    fun toRadians(dy: Double, dx: Double, zeroPoint: String = "E", posDirClockwise: Boolean = false) : Double {
        if (zeroPoint !in validZeroPoints) {
            throw IllegalArgumentException("Zero point argument '$zeroPoint' is invalid. Must be in $validZeroPoints")
        }

        var tan = atan2(dy, dx)

        when (zeroPoint) {
            "N" -> tan -= Math.PI / 2
            "W" -> tan += Math.PI
            "S" -> tan += Math.PI / 2
            "NE" -> tan -= Math.PI / 4
            "NW" -> tan -= (Math.PI * 3 / 4)
            "SE" -> tan += Math.PI / 4
            "SW" -> tan += (Math.PI * 3 / 4)
        }

        if (posDirClockwise) {
            tan *= -1
        }

        if (tan < 0) {
            tan += (Math.PI * 2)
        }

        return tan
    }

    fun toRadians(dy: Int, dx: Int, zeroPoint: String = "E", posDirClockwise: Boolean = false) : Double {
        return toRadians(dy.toDouble(), dx.toDouble(), zeroPoint, posDirClockwise)
    }

    /** Returns the distance between two 2D points. */
    fun distance(x: Int, y: Int, x2:Int, y2:Int) : Double {
        return distance(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
    }

    fun distance(x: Double, y: Double, x2:Double, y2:Double) : Double {
        val dx = (x - x2)
        val dy = (y - y2)
        return sqrt((dx * dx) + (dy * dy))
    }

    /** Returns the distance between two 3D points. */
    fun distance(x: Int, y: Int, z:Int, x2:Int, y2:Int, z2:Int) : Double {
        return distance(x.toDouble(), y.toDouble(), z.toDouble(), x2.toDouble(), y2.toDouble(), z2.toDouble())
    }

    fun distance(x: Double, y: Double, z: Double, x2:Double, y2:Double, z2:Double) : Double {
        val dx = (x - x2)
        val dy = (y - y2)
        val dz = (z - z2)
        return sqrt((dx * dx) + (dy * dy) + (dz * dz))
    }

    /** Returns a list of unique pairs from the given list. */
    fun <T> uniquePairs(a: List<T>): List<Pair<T, T>> {
        return uniquePairsSequence(a).toList()
    }

    /** Returns a list of unique pairs from the given list. */
    fun <T> uniquePairsSequence(a: List<T>) = sequence {
        for (i in a.indices) {
            for (j in (i + 1 until a.size)) {
                yield(Pair(a[i], a[j]))
            }
        }
    }

    /** Returns a list of unique triples from the given list. */
    fun <T> uniqueTriples(a: List<T>): List<Triple<T, T, T>> {
        return uniqueTriples(a).toList()
    }

    fun <T> uniqueTriplesSequence(a: List<T>) = sequence {
        for (i in a.indices) {
            for (j in (i + 1 until a.size)) {
                for (k in (j + 1 until a.size)) {
                    yield(Triple(a[i], a[j], a[k]))
                }
            }
        }
    }

    /** Returns a list of permutations of the given list. */
    fun <T> perms(list: List<T>): List<List<T>> {
        if (list.isEmpty()) return emptyList()

        val result: MutableList<List<T>> = mutableListOf()
        for (i in list.indices) {
            perms(list - list[i]).forEach{ item -> result.add(item + list[i]) }
        }
        return result
    }

    /** Returns an int range from the given collection extracted by the given selectors. */
    fun <T> rangeInt(
        c: Collection<T>,
        minSelector: (T) -> Int,
        maxSelector: (T) -> Int
    ): IntProgression {
        return c.minOf(minSelector)..c.maxOf(maxSelector)
    }

    /** Returns an long range from the given collection extracted by the given selectors. */
    fun <T> rangeLong(
        c: Collection<T>,
        minSelector: (T) -> Long,
        maxSelector: (T) -> Long
    ): LongProgression {
        return c.minOf(minSelector)..c.maxOf(maxSelector)
    }

    /**
     * Tokenizes the given string using selectors and prioritizing associations.
     *
     * Ex: "a + b + (c + (d * e))" with a ' ' separator and associations '()' would return the list
     * ["a", "+", "b", "+", "( c + ( d * e ) )"].
     */
    fun toTokens(s: String, separator: String = " ", associations: String = "", ignoreCase: Boolean = false)
            : List<String> {
        val tokens = mutableListOf<String>()
        val ascPairs = mutableListOf<Pair<Char, Char>>()
        for (i in associations.indices step 2) {
            ascPairs.add(Pair(associations[i], associations[i + 1]))
        }

        val it = s.iterator()

        var curr = ""
        var ascClose = '0'
        var ascCount = 0

        while (it.hasNext()) {
            val c = it.next()

            if (ascCount != 0) {
                // Keep pulling into the association until it is complete.
                curr += c

                if (c == curr[0]) {
                    ascCount++
                } else if (c == ascClose) {
                    ascCount--

                    if (ascCount == 0) {
                        tokens.add(curr)
                        curr = ""
                        ascClose = '0'
                    }
                }
            } else {
                // Add to the token thing looking for associative brackets and separators
                val asc = ascPairs.firstOrNull { it.first == c }
                if (asc != null) {
                    // Create a new association
                    if (curr.isNotEmpty()) {
                        tokens.add(curr)
                    }
                    ascClose = asc.second
                    curr = "$c"
                    ascCount++
                    continue
                }

                // Accumulate in the current token
                curr += c
                if (separator.isNotEmpty() && curr.endsWith(separator, ignoreCase = ignoreCase)) {
                    curr = curr.substring(0, curr.length - separator.length)

                    if (curr.isNotEmpty()) {
                        tokens.add(curr)
                        curr = ""
                    }
                }
            }
        }

        // Add the last token
        if (curr.isNotEmpty()) {
            tokens.add(curr)
        }

        return tokens
    }
}