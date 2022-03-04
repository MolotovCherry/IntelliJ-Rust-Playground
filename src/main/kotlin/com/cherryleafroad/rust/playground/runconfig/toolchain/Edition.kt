package com.cherryleafroad.rust.playground.runconfig.toolchain

enum class Edition(val index: Int, val myName: String) {
    DEFAULT(0, "default"),
    EDITION_2015(1, "2015"),
    EDITION_2018(2, "2018"),
    EDITION_2021(3, "2021");

    override fun toString(): String = myName

    companion object {
        fun fromIndex(index: Int): Edition = values().find { it.index == index } ?: DEFAULT
    }
}
