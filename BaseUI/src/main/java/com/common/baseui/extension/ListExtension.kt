package com.common.baseui.extension

fun <T> ArrayList<T>.addOnlyOne(elementNew: T, sameIf: (T) -> Boolean?) {
    var isContain = false
    for(i in 0 until size) {
        val item = getOrNull(i)
        if (item != null && (sameIf.invoke(item) == true || item == elementNew)) {
            isContain = true
        }
    }
    if (!isContain) add(elementNew)
}

inline fun <T> MutableList<T>.removeIF(predicate: (T) -> Boolean): Boolean {
    if (isEmpty()) return false
    var remove = false
    for (index in size -1 downTo 0) {
        if (predicate.invoke(get(index))) {
            removeAt(index)
            remove = true
        }
    }
    return remove
}