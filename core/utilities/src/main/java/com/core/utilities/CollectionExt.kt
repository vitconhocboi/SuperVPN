package com.core.utilities

infix fun <T> List<T>.equalsIgnoreOrder(other: List<T>) = this.size == other.size
        && this.toSet() == other.toSet()

fun <T> ArrayList<T>.customRemoveLast(): T? {
    if (isEmpty()) {
        return null
    }
    // removeAt sẽ xóa phần tử tại chỉ mục và trả về chính phần tử đó
    return removeAt(size - 1)
}


fun <T> ArrayList<T>.customRemoveFirst(): T? {
    if (isEmpty()) {
        return null
    }
    // removeAt sẽ xóa phần tử tại chỉ mục và trả về chính phần tử đó
    return removeAt(0)
}