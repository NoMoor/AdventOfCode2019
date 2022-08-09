fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay4.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay4 {
    private const val input = "d4.txt"

    fun doDay() {
        val lines = Utils.readFile(input)

        val range = 307237..769058

        var result = range.count { i ->
            var increasing = i.toString().toList().windowed(2).all { it[0] <= it[1] }
            var double = i.toString().groupBy { it }.any { it.value.size > 1 }
            increasing && double
        }

        println("Result $result")
    }
}