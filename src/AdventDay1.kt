fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay1.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

// https://adventofcode.com/2019/day/1
object AdventDay1 {
    private const val input = "d1.txt"

    fun doDay() {
        val lines = Utils.readFile(input)
            .map {
                var total = 0L
                var fuel = (it.toLong() / 3) - 2
                while (fuel > 0) {
                    total += fuel
                    fuel = (fuel / 3) - 2
                }
                total
            }
            .sum()

        println("Input:\n_______\n${lines}\n_______")

        // high 4842834
    }
}