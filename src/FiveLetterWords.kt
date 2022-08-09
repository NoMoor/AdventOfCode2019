@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.math.pow
import kotlinx.coroutines.*
import java.util.concurrent.Executors

val dispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

fun main() = runBlocking(dispatcher) {
    val s = StopWatch.createStarted()
    FiveLetterWords.doDay()

    s.stop(msg = "Execution Complete: ")
}

@ExperimentalStdlibApi
object FiveLetterWords {
    private const val input = "words_alpha.txt"

    suspend fun doDay() = coroutineScope {
        val stopWatch = StopWatch.createStarted()

        // Read the lines from file
        val lines = Utils.readFile(input)
        stopWatch.lap("Input: ${lines.size}")

        // Filter to words that have length 5 and only unique letters
        val fiveLetterAnagrams = lines.filter { it.length == 5 }
            .groupBy { it.lowercase().toList().sumOf { 2.0.pow((it - 'a')).toInt() } }
            .filterKeys { it.toString(2).count { '1' == it } == 5 }
            .map { Anagrams(it.key, it.value) }
        log("Five Letter Words with unique letters: ${fiveLetterAnagrams.size}")

        val results = fiveLetterAnagrams.indices.chunked(10)
            .map { async { doPart(fiveLetterAnagrams, it) } }
            .flatMap { it.await() }

        stopWatch.stop("List Complete: ${results.size}")
    }

    private fun doPart(anagrams: List<Anagrams>, wordRange: List<Int>): List<Combination> {
        val newWatch = StopWatch.createStarted()
//        log("Start [${wordRange.first()}, ${wordRange.last()}]")

        // For Each 'starting word' in the range we are checking
        // Filter the remaining words to ones that can be paired with no letter in common.
        // Compute the 'letter mask', the int representing which letters are present in the combined word set.
        // Repeat this process until we have 5 words total with no overlapping letters or until such a set is
        // impossible because we have run out of words.
        val combinations = mutableListOf<Combination>()
        for (oneWordIndex in wordRange) {
            val oneWord = anagrams[oneWordIndex]

            val oneWordMask = oneWord.letterMask
            val twoWordList = filterList(anagrams, oneWordIndex, oneWordMask)

            for ((twoWordIndex, twoWord) in twoWordList.withIndex()) {
                val twoWordMask = oneWord.letterMask or twoWord.letterMask
                val threeWordList = filterList(twoWordList, twoWordIndex, twoWordMask)

                for ((threeWordIndex, threeWord) in threeWordList.withIndex()) {
                    val threeWordMask = twoWordMask or threeWord.letterMask
                    val fourWordList = filterList(threeWordList, threeWordIndex, threeWordMask)

                    for ((fourWordIndex, fourWord) in fourWordList.withIndex()) {
                        val fourWordMask = threeWordMask or fourWord.letterMask
                        val fiveWordList = filterList(fourWordList, fourWordIndex, fourWordMask)

                        for (fiveWord in fiveWordList) {
                            val fiveWordMask = fourWordMask or fiveWord.letterMask
                            val newCombination = Combination(fiveWordMask, buildList {
                                add(oneWord.words)
                                add(twoWord.words)
                                add(threeWord.words)
                                add(fourWord.words)
                                add(fiveWord.words)
                            })

                            combinations += newCombination
                        }
                    }
                }
            }
        }
        newWatch.stop("Done [${wordRange.first()}, ${wordRange.last()}]")
        return combinations
    }

    /**
     * Filters the list of anagrams by first creating a sublist from [index, end] and then selecting words that have
     * no letters matching the given letter mask.
     */
    private fun filterList(
        list: List<Anagrams>,
        index: Int,
        letterMask: Int
    ) = list.asSequence() // Call as sequence first since it will make the 'drop' call much faster.
        .drop(index)
        .filter { letterMask and it.letterMask == 0 }
        .toList()

    /** Data object to hold a mask of letters and the assoiated words. */
    data class Anagrams(val letterMask: Int, val words: List<String>)

    /** Data object to hold a mask of letters and the list of word lists that can be combined to create that mask. */
    data class Combination(val letterMask: Int, val wordGroups: List<List<String>>)
}

/** Stopwatch class for basic timing functions. */
class StopWatch {
    var startTime = -1L
    var endTime = -1L

    companion object {
        fun createStarted() : StopWatch {
            return StopWatch().start()
        }
    }

    fun start() : StopWatch {
        startTime = System.nanoTime()
        return this
    }

    fun lap(msg: Any = "", log: Boolean = false) {
        logTime(msg, log)
        startTime = System.nanoTime()
    }

    fun stop(msg: Any = "", log: Boolean = false) {
        logTime(msg, log)
        endTime = System.nanoTime()
    }

    private fun logTime(msgAny: Any = "", doLog: Boolean) {
        val msg = msgAny.toString()
        if (doLog || msg.isNotEmpty()) {
            log("${msg.ifEmpty { "Time:" }} ${(System.nanoTime() - startTime) / 1000000}")
        }
    }
}

/** Prints to console with the thread (and coroutine info) as the prefix fixed prefix. */
private fun log(msg: Any) = println("[${Thread.currentThread().name}] $msg")