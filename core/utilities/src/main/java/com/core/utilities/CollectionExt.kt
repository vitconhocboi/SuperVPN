package com.core.utilities

infix fun <T> List<T>.equalsIgnoreOrder(other: List<T>) = this.size == other.size
        && this.toSet() == other.toSet()