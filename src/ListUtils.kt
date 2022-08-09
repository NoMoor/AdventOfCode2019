object ListUtils {

    fun <T> rotateClockwise(image: List<List<T>>, times: Int = 1) : MutableList<MutableList<T>> {
        var tmp = image.map { it.toMutableList() }.toMutableList()

        (0..times).forEach {
            tmp = rotateClockwiseInternal(tmp)
        }

        return tmp
    }

    private fun <T> rotateClockwiseInternal(image: List<List<T>>) : MutableList<MutableList<T>> {
        var temp = mutableListOf<MutableList<T>>()
        for (i in image.indices) {
            var s = mutableListOf<T>()
            for (j in image[0].indices) {
                s.add(image[image.size - 1 - j][i])
            }
            temp.add(s)
        }
        return temp
    }

    fun <T> flipHorizontal(image: List<List<T>>) : List<List<T>> {
        return image.map { it.reversed() }.toList()
    }

    fun <T> flipVertical(image: List<List<T>>) : List<List<T>> {
        return image.reversed()
    }
}