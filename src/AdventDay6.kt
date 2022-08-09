fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay6.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay6 {
    private const val input = "d6.txt"

    fun doDay() {
        val mToP = Utils.readFile(input).associate {
            val p = it.split(")")

            Pair(p[1], p[0])
        }

        println(mToP.keys.sumOf { path(mToP, it).size })

        val y = path(mToP, "YOU")
        val s = path(mToP, "SAN")

        while (y.removeLast() == s.removeLast()) {

        }
        println(y.size + s.size)
    }

    private fun path(mToP: Map<String, String>, it: String): MutableList<String> {
        val p = mutableListOf<String>()
        var c = it
        while (mToP.containsKey(c)) {
            p += c
            c = mToP[c]!!
        }

        return p
    }
}