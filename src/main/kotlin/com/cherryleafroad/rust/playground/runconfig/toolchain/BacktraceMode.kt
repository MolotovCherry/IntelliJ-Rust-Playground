package com.cherryleafroad.rust.playground.runconfig.toolchain

enum class BacktraceMode(val index: Int, private val title: String) {
    NO(0, "No"),
    SHORT(1, "Short"),
    FULL(2, "Full");

    override fun toString(): String = title

    companion object {
        val DEFAULT: BacktraceMode = SHORT
        fun fromIndex(index: Int): BacktraceMode = values().find { it.index == index } ?: DEFAULT
    }
}
