fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay5.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay5 {
    private const val input = "d5.txt"

    fun doDay() {
        val lines = Utils.readFile(input)[0].split(",").map { it.toLong() }.toMutableList()

        var output = Mission(lines).execute(listOf(0, 5))

        println("Input:\n_______\n${output}\n_______")
    }
}