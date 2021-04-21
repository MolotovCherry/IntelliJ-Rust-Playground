package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
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
    val check: Boolean = false,
    val clean: Boolean = false,
    val expand: Boolean = false,
    val infer: Boolean = false,
    val quiet: Boolean = false,
    val release: Boolean = false,
    val test: Boolean = false,
    val verbose: Boolean = false,
    val toolchain: RustChannel = Settings.getSelectedToolchain(),

    val cargoOption: MutableList<String> = mutableListOf(),
    val edition: Edition = Edition.DEFAULT,
    val mode: String = "",

    val src: List<String> = listOf(),
    val args: List<String> = listOf(),
    val playArgs: List<String> = listOf(),

    val runCmd: List<String> = listOf(),
    val buildCmd: List<String> = listOf(""),

    // controls whether to use the build screen
    val runBuild: Boolean = false,
    val runRun: Boolean = false,
    val cleanAndRun: Boolean = false,
    val cleanSingle: Boolean = false,
    val cleanAndRunCmd: List<String> = listOf("")
)
