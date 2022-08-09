fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay10.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay10 {
    private const val input = "d10.txt"

    fun doDay() {
        val lines = Utils.readFile(input)

        // Create coordinates
        val coords = mutableSetOf<Coord>()
        for (y in lines.indices) {
            for (x in lines[y].indices) {
                if (lines[y][x] == '#') {
                    coords += Coord(x, y)
                }
            }
        }

        println("Input:\n_______\n${lines}\n_______")

        // Determine los for each asteroid
        val los = mutableMapOf<Coord, Map<Double, List<Coord>>>()
        coords.forEach {
            val c = it
            los[c] = coords
                .filter { it != c }
                .groupBy {
                    var dx = it.x - c.x
                    var dy = c.y - it.y

                    Utils.toRadians(dy, dx, zeroPoint = "N", posDirClockwise = true)
                }
        }

        val v = los.maxByOrNull { it.value.size }!!
        println("Optimal point: ${v.key}")
        println(v.value.size)

        // For the best fit, create an ordered list of the polar coordinates with asteroids ordered by distance away
        var laserOrder = v.value
            .map { p ->
                Pair(p.key, p.value.sortedBy { v.key.distance(it) }.toMutableList())
            }
            .sortedBy { it.first }
            .toMutableList()

        // Create the laser order list
        var lo = mutableListOf<Coord>()
        while (laserOrder.isNotEmpty()) {
            for (a in laserOrder) {
                lo += a.second.removeFirst()
            }
            laserOrder.removeAll { it.second.isEmpty() }
        }

        println(lo[199].x * 100 + lo[199].y)
    }

    data class Coord(val x: Int, val y: Int) {
        override fun toString(): String {
            return "[$x, $y]"
        }

        fun distance(other: Coord) : Double {
            return Utils.distance(x, y, other.x, other.y)
        }
    }
}