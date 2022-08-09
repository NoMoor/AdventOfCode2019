fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay8.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay8 {
    private const val input = "d8.txt"

    val width = 25
    val height = 6
    val layerSize = width * height

    fun doDay() {
        val ls = Utils.readFile(input)[0].chunked(layerSize)

        val min = ls.minByOrNull { it.count { it == '0' } }!!

        val result = min.count { it == '1' } * min.count { it == '2' }
        println(result)

        for (y in 0 until height) {
            var xs = ""
            for (x in 0 until width) {
                val index = x + (y * width)
                xs += if (ls.map { it[index] }.first { it != '2' } == '1') " " else "#"
            }

            println(xs)
        }
    }
}
