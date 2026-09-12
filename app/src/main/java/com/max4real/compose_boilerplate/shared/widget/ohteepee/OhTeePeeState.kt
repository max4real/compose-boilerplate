package com.max4real.compose_boilerplate.shared.widget.ohteepee

data class OhTeePeeState(
    val digits: List<String> = List(6) { "" }
) {
    fun update(index: Int, value: String): OhTeePeeState {
        val newDigits = digits.toMutableList()
        newDigits[index] = value
        return copy(digits = newDigits)
    }

    fun isComplete(): Boolean = digits.none { it.isBlank() }

    fun asString(): String = digits.joinToString("")
}