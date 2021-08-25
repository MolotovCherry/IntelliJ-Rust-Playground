package com.cherryleafroad.rust.playground.runconfig.toolchain

enum class RustChannel(val index: Int, val channel: String) {
    DEFAULT(0, "default"),
    STABLE(1, "stable"),
    BETA(2, "beta"),
    NIGHTLY(3, "nightly"),
    DEV(4, "dev");

    override fun toString(): String = channel

    companion object {
        fun fromIndex(index: Int): RustChannel = values().find { it.index == index } ?: DEFAULT
    }
}
