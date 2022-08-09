@file:OptIn(ExperimentalStdlibApi::class)

import kotlin.math.pow
import kotlinx.coroutines.*
import java.util.concurrent.Executors

/**
 * Implementation of a problem specified by [Standup Maths](https://www.youtube.com/watch?v=_-AfhLQfb6w).
 *
 * The implementation works off the following ideas:
 * 1. We can create a 'letter-mask' of each word where each bit represents whether a given letter is present. The mask
 *    is formed by taking each letter, converting it to a digit (ie. a = 0, b = 1, c = 2), and the bit-shifting that
 *    amount. Given 26 letters in the English alphabet, the result of summing these individual character bits gives a
 *    number that can fit in an Int.
 * 2. We can check if two words have any overlapping letters by AND-ing these 'letter masks' and seeing if any bits
 *    are 1 (ie. the result is non-zero).
 * 3. We can create a new 'letter mask' for a set of words by OR-ing the 'letter masks' of the individual words.
 * 4. We can decompose the problem by assuming one word is in the set and trying to find a combination of 4 words
 *    with no overlapping letters using the remaining words in the list. Doing this would give us a run-time of O(n^5)
 *    since we have to run deep for-loops over the whole set for each word we are trying to find.
 * 5. We can improve the average run-time of this by aggressively pruning invalid combinations at each stage and
 *    filtering the remaining word list to only those that have no letters matching the set we have selected. The
 *    exact analysis of this is likely outside the scope of documentation here as it depends on the letter frequency.
 *
 *    As a first approximation, assume all letters are equally likely in our word set, filtering the set of (n - 1)
 *    words is an O(n) operation, looping over the words and AND-ing each mask with the mask of the selected word
 *    (described above). Each word has a ~66% chance of being filtered out at the first run `1 - ((1 - (5/26)) ^ 5)`.
 *    An O(n) process which removes > 50% of the elements is at worst O(n * log n) to check if a given word is part of
 *    a 5-word solution.
 *
 *    If, instead of doing this pruning, we created all combinations of words in the set and checked at the end to see
 *    if the set was valid, we would have n^4 checks for each first word. Given a set of 100 words, this is 100_000_000
 *    checks.
 * 6. We can improve this execution time by realizing that we don't need to iterate the full set for every word because
 *    we don't care about the order of the results.
 *
 *    To intuit this, assume we had a list from [0 .. 3] and we wanted to print all combinations of pairs of elements.
 *    This could be implemented as nested loop where we pick the first element and then a nested loop to pick the
 *    second. The first loop would print all pairs with the number 0 -> [[0, 1], [0, 2], [0, 3]]. The second iteration
 *    of the loop would print all pairs with the number 1 present. But, notice the first loop already printed [0, 1]
 *    and since we don't care about order, we don't want to print [1, 0]. As a result, the second loop only prints
 *    [[1, 2], [1, 3]]. The code for this would look something like:
 *
 *    ```
 *    val nums = (0..3).toList()
 *
 *    for (i in nums.indices) {
 *        for (j in (i + 1) until nums.size) {
 *          println(Pair(nums[i], nums[j]))
 *        }
 *    }
 *    ```
 *
 *    This loop, with each subsequent iteration has a significantly faster run time since the inner loop gets smaller
 *    with each iteration of the outer loop. The first iteration has n accesses the last just 1 resulting in
 *    (n + (n-1) + n-2 + .. + 2 + 1). The O-notation of this is unchanged — still O(n^2) — since we have n elements
 *    with an average size of n / 2 resulting in n * n / 2. Nonetheless, this is an execution improvement of 50%.
 *
 * The following solution also experiments with kotlinx coroutine libraries. Because each word can be treated
 * independently, this can be solved in parallel, on separate threads, To run this, ensure that
 * `jetbrains.kotlinx.coroutines.core.jvm` maven library is included in your project.
 */
@ExperimentalStdlibApi
fun main() = runBlocking(dispatcher) {
    val s = StopWatch.createStarted()
    FiveLetterWords.doDay()

    s.stop(msg = "Execution Complete: ")
}

const val chunkSize = 10
const val threadCount = 8

val dispatcher = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()

@ExperimentalStdlibApi
object FiveLetterWords {
    private const val input = "words_alpha.txt"

    suspend fun doDay() = coroutineScope {
        val stopWatch = StopWatch.createStarted()

        // Read the lines from file
        val lines = Utils.readFile(input)
        stopWatch.lap("Input: ${lines.size}")

        // Filter to words that have length 5
        val fiveLetterAnagrams = lines.filter { it.length == 5 }
            // Group words by those that have the same letter-mask
            .groupBy { word -> word.lowercase().toList().sumOf { 2.0.pow((it - 'a')).toInt() } }
            // Filter to words that only have unique letters by counting the number of 1's in the letter-mask
            .filterKeys { key -> key.toString(2).count { '1' == it } == 5 }
            // Create Anagram objects from this map
            .map { Anagrams(it.key, it.value) }
        log("Five Letter Words with unique letters: ${fiveLetterAnagrams.size}")

        // Segment the list of anagrams and find solutions containing those elements in parallel.
        val results = fiveLetterAnagrams.indices.chunked(chunkSize)
            .map { async { doPart(fiveLetterAnagrams, it) } }
            .flatMap { it.await() }

        stopWatch.stop("List Complete: ${results.size}")
    }

    /**
     * For Each 'starting word' in the range we are checking:
     *   1) Filter the remaining words to ones that can be paired with no letter in common.
     *   2) Compute the 'letter mask', the int representing which letters are present in the combined word set.
     *   3) Repeat this process until we have 5 words total with no overlapping letters or until such a set is
     *      impossible because we have run out of words.
     * Return the list of solutions
     */
    private fun doPart(anagrams: List<Anagrams>, wordRange: List<Int>): List<Combination> {
        val newWatch = StopWatch.createStarted()

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

    /** Data object to hold a mask of letters and the associated words. */
    data class Anagrams(val letterMask: Int, val words: List<String>)

    /** Data object to hold a mask of letters and the list of word lists that can be combined to create that mask. */
    data class Combination(val letterMask: Int, val wordGroups: List<List<String>>)
}

/** Stopwatch class for basic timing functions. */
class StopWatch {
    private var startTime = -1L
    private var endTime = -1L

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