package framboos.vrolijke.jdriven.com.utils

fun <E> List<E>.getNextOrNull(element: E): E? {
    val index = indexOf(element)
    return if (index != -1 && index < size - 1) this[index + 1] else null
}
