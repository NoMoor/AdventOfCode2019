import java.lang.IllegalStateException

class Mission(l: MutableList<Long>) {

    var code = mutableMapOf<Long, Long>()

    init {
        println("Input size: ${l.size}")
        l.forEachIndexed { index, l -> code[index.toLong()] = l }
    }

    var pointer = 0L
    var output = -1L
    var inCounter = 0
    var relBase = 0

    // Must be a bug in the execute code...

    fun execute(inputImmutable: List<Long> = listOf(0L), loud: Boolean = false) : Pair<Long, Boolean> {
        val input = inputImmutable.toMutableList()

        while (pointer < code.size) {
            var raw = code[pointer++] ?: 0
            val mode3 = raw / 10000 % 10
            val mode2 = raw / 1000 % 10
            val mode1 = raw / 100 % 10

            val op = raw % 100

            p("$pointer) ParamMode: $mode1|$mode2|$mode3 Op: $op", loud)

            when (op.toInt()) {
                1 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)
                    val p3 = getPtr(pointer++, mode3, code)
                    code[p3] = p1 + p2
                }
                2 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)
                    val p3 = getPtr(pointer++, mode3, code)
                    code[p3] = p1 * p2
                }
                3 -> {
                    var p1 = getPtr(pointer++, mode1, code)

                    val inputVal = input.removeFirst()
                    p("Store $inputVal at $p1", loud)
                    code[p1] = inputVal
                    inCounter++
                }
                4 -> {
                    output = get(pointer++, mode1, code)

                    p("Output $output from ${pointer - 1} $mode1", loud)
                    return Pair(output, false)
                }
                5 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)

                    if (p1 != 0L) {
                        pointer = p2
                    }
                }
                6 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)

                    if (p1 == 0L) {
                        pointer = p2
                    }
                }
                7 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)
                    val p3 = getPtr(pointer++, mode3, code)

                    p("Compare $p1 to $p2", loud)

                    code[p3] = if (p1 < p2) 1 else 0
                }
                8 -> {
                    val p1 = get(pointer++, mode1, code)
                    val p2 = get(pointer++, mode2, code)
                    val p3 = getPtr(pointer++, mode3, code)

                    p("Compare $p1 to $p2", loud)

                    code[p3] = if (p1 == p2) 1 else 0
                }
                9 -> {
                    val p1 = get(pointer++, mode1, code).toInt()

                    p("Adjust rel base $relBase by $p1", loud)

                    relBase += p1
                }
                99 -> {
                    // Set the pointer back to where it was.
                    pointer--

                    p("Final output $output", loud)
                    return Pair(output, true)
                }
                else -> {
                    throw IllegalStateException("The fuck?!")
                }
            }
        }

        throw IllegalArgumentException("Got here somehow... $pointer")
    }

    fun p(s: String, b: Boolean = false) {
        if (b) {
            println(s)
        }
    }

    private fun get(pointer: Long, mode: Long, lines: Map<Long, Long>) : Long {
        var ptr = getPtr(pointer, mode, lines)
        if (mode == 0L) {
            return lines[ptr]!!
        } else if (mode == 1L) {
            return ptr
        } else if (mode == 2L) {
            return lines[ptr]!!
        }

        throw IllegalArgumentException("Unknown mode $mode")
    }

    private fun getPtr(pointer: Long, mode: Long, lines: Map<Long, Long>) : Long {
        var p = lines[pointer]!!
        if (mode == 0L) {
            return p
        } else if (mode == 2L) {
            return (relBase + p)
        } else if (mode == 1L) {
            return p
        }

        throw IllegalArgumentException("Unknown mode $mode")
    }
}