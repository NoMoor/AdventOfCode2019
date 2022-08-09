import kotlin.math.min

fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay2.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay2 {
    private const val input = "d2.txt"

    fun doDay() {
        val lines = Utils.readFile(input)[0].split(",").map { it.toLong() }.toMutableList()

        for (noun in (0 .. 100)) {
            for (verb in (0 .. 100)) {
                val copy = ArrayList(lines)
                copy[1] = noun.toLong()
                copy[2] = verb.toLong()

                var result = Mission(ArrayList(copy)).execute(listOf(0)).first
                println("Try $noun / $verb = $result")

                if (result == 19690720L) {
                    println("Result ${100 * noun + verb}")
                    return
                }
            }
        }
    }
}