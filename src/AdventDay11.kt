fun main() {
    val timeMicros = kotlin.system.measureNanoTime {
        AdventDay11.doDay()
    }
    val durationMs = timeMicros * .000001
    println("Time to run: $durationMs ms")
}

object AdventDay11 {
    private const val input = "d11.txt"

    fun doDay() {
        val lines = Utils.readFile(input)[0].split(",").map { it.toLong() }.toMutableList()

        val loc = Loc()
        val m = Mission(lines)
        var result: Pair<Long, Boolean>
        var paintedPanels = mutableSetOf<Coord>()
        var whitePanels = mutableSetOf<Coord>()
        whitePanels.add(loc.pos)
        var movements = 0

        do {
            var incolor = if (whitePanels.contains(loc.pos)) 1L else 0L
            result = m.execute(listOf(incolor))

            if (!result.second) {
                val outcolor = result.first
                if (outcolor == 1L) {
                    whitePanels.add(loc.pos)
                } else {
                    whitePanels.remove(loc.pos)
                }
                paintedPanels.add(loc.pos)

                result = m.execute(listOf(incolor))
                if (result.first == 0L) {
                    loc.turnLeft()
                } else {
                    loc.turnRight()
                }
                loc.moveForward()
                movements++
                println("New Location: ${loc.pos}")
            }
        } while (!result.second)

        println("Input:\n_______\n${lines}\n_______")
        println(paintedPanels.size) // 2253 too high
        println(whitePanels.size)
        println(movements)

        var xs = Utils.rangeInt(paintedPanels, {it.x}, {it.x})
        var ys = Utils.rangeInt(paintedPanels, {it.y}, {it.y})

        println(xs)
        println(ys)

        for (y in ys.reversed()) {
            var l = ""
            for (x in xs) {
                val c = Coord(x, y)
                if (whitePanels.contains(c)) {
                    l += "W"
                } else if (paintedPanels.contains(c)) {
                    l += " "
                } else {
                    l += " "
                }
            }

            println(l)
        }
    }

    class Loc {
        var pos = Coord(0, 0)
        var forward = Coord(0, 1)

        fun turnLeft() {
            forward = Coord(-forward.y, forward.x)
        }

        fun turnRight() {
            forward = Coord(forward.y, -forward.x)
        }

        fun moveForward() {
            pos += forward
        }
    }

    data class Coord(val x: Int, val y: Int) {
        operator fun plus(other: Coord) : Coord {
            return Coord(x + other.x, y + other.y)
        }
    }
}