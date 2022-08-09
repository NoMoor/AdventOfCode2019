fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay7.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay7 {
    private const val input = "d7.txt"

    fun doDay() {
        val lines = Utils.readFile(input)[0].split(",").map { it.toLong() }.toMutableList()

        println("Input\n-------------")

        val orders = Utils.perms((5..9).toList())

        var maxOutput = listOf<Int>()
        var maxVal = 0
        for (order in orders) {
            val outputs = IntArray(5)
            var missionList = MutableList(order.size) { Mission(copy(lines)) }

            var done = false
            while (!done) {
                println("Try $order Outputs: ${outputs.contentToString()}")
                for (i in order.indices) {
                    val input = order[i]
                    val m = missionList[i]

                    val output = outputs[if (i - 1 < 0) outputs.size - 1 else i - 1]

                    val result = m.execute(listOf(input.toLong(), output.toLong()))
                    outputs[i] = result.first.toInt()
                    done = result.second
                }
            }
            if (outputs.last() > maxVal) {
                maxOutput = order
                maxVal = outputs.last()
            }
        }

        println("-------------")

        println(maxOutput)
        println(maxVal)
    }

    fun copy(lines: MutableList<Long>) : MutableList<Long> {
        return ArrayList(lines)
    }

    fun <T> copy(lines: MutableSet<T>) : MutableSet<T> {
        return HashSet(lines)
    }
}