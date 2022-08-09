fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay9.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay9 {
    private const val input = "d9.txt"

    fun doDay() {
        val lines = Utils.readFile(input)

        var m = Mission(lines[0].split(",").map { it.toLong() }.toMutableList())
        var output: Pair<Long, Boolean>
        do {
            output = m.execute(listOf(2))
            println("Output: $output")
        } while (!output.second)

        println("Input:\n_______\n${lines}\n_______")
        println(output)

        // Too low
    }
}