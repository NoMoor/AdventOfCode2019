import kotlin.math.abs

fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay3.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay3 {
    private const val input = "d3.txt"

    val dirs = mutableMapOf<Char, Coord>()

    init {
        dirs['R'] = Coord(1, 0)
        dirs['L'] = Coord(-1, 0)
        dirs['U'] = Coord(0, 1)
        dirs['D'] = Coord(0, -1)
    }

    fun doDay() {
        val lines = Utils.readFile(input).map { it.split(",") }

        var coords = mutableMapOf<Coord, Int>()
        var intersect = mutableSetOf<Pair<Coord, Int>>()

        var loc = Coord(0, 0)
        var timing = 0

        for (s in lines[0]) {
            val d = s[0]
            val c = s.substring(1).toInt()
            println("$d $c")

            for (i in (0 until c)) {
                timing++
                loc += dirs[d]!!
                if (loc !in coords) {
                    coords.put(loc, timing)
                }
            }
        }

        loc = Coord(0, 0)
        timing = 0
        for (s in lines[1]) {
            val d = s[0]
            val c = s.substring(1).toInt()
            println("$d $c")

            for (i in (0 until c)) {
                timing++
                loc += dirs[d]!!
                if (loc in coords) {
                    intersect += Pair(loc, coords[loc]!! + timing)
                }
            }
        }

        // 13370 is too high
        intersect.forEach { println(it) }

        val min = intersect.minOf { abs(it.first.x) + abs(it.first.y) }
        val minTiming = intersect.minOf { abs(it.second) }

        println("Input:\n_______\n${min}\n_______")
        println("min timing $minTiming")
    }

    data class Coord(val x: Int, val y: Int) {

        operator fun plus(other: Coord) : Coord {
            return Coord(x + other.x, y + other.y)
        }
    }
}