package com.cherryleafroad.rust.playground

import org.rust.cargo.toolchain.RustChannel

enum class Edition(val index: Int, val myName: String) {
    DEFAULT(0, "DEFAULT"),
    EDITION_2015(1, "2015"),
    EDITION_2018(2, "2018");
    //EDITION_2021(3, "2021"); -> not yet released

    companion object {
        fun fromIndex(index: Int): Edition = values().find { it.index == index } ?: DEFAULT
    }
}

data class ParserResults(
    val check: Boolean,
    val clean: Boolean,
    val expand: Boolean,
    val infer: Boolean,
    val quiet: Boolean,
    val release: Boolean,
    val test: Boolean,
    val verbose: Boolean,
    val toolchain: RustChannel,

    val cargoOption: List<String>,
    val edition: Edition,
    val mode: String,

    // args to pass various parts
    val src: List<String>,
    val args: List<String>,

    val runCmd: List<String>,
    val finalCmd: List<String>
)
