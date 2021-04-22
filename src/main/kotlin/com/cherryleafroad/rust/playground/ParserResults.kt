package com.cherryleafroad.rust.playground

import org.rust.cargo.toolchain.RustChannel

enum class Edition(val index: Int, val myName: String) {
    DEFAULT(0, "DEFAULT"),
    _2015(1, "2015"),
    _2018(2, "2018");

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

    val onlyRun: Boolean,

    val cargoOption: List<String>,
    val edition: Edition,
    val mode: String,

    // args to pass various parts
    val src: List<String>,
    val args: List<String>,

    // build vs run command
    val runCmd: List<String>,
    val buildCmd: List<String>,
    val buildCmd2: List<String>,

    // controls whether to use the build screen
    val runBuild: Boolean,
    val runBuild2: Boolean,
    val runRun: Boolean,

    val cleanSingle: Boolean,
    val cleanAndRun: Boolean,
    val cleanCmd: List<String>,

    val finalBuildCmd: List<String>,
    val finalRunCmd: List<String>,
    val finalBuildCmd2: List<String>
)
