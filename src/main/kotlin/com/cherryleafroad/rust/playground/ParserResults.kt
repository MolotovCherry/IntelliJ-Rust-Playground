package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import org.rust.cargo.toolchain.RustChannel

data class ParserResults(
    var check: Boolean = false,
    var clean: Boolean = false,
    var expand: Boolean = false,
    var infer: Boolean = false,
    var quiet: Boolean = false,
    var release: Boolean = false,
    var test: Boolean = false,
    var verbose: Boolean = false,
    var toolchain: RustChannel = Settings.getSelectedToolchain(),

    val cargoOption: MutableList<String> = mutableListOf(),
    var edition: String? = null,
    var mode: String? = null,

    val src: MutableList<String> = mutableListOf(),
    val args: MutableList<String> = mutableListOf(),
    val playArgs: MutableList<String> = mutableListOf(),

    var runCmd: MutableList<String> = mutableListOf(),
    var buildCmd: MutableList<String> = mutableListOf("play"),

    var lineOffset: Int = 0,

    var noMatches: Boolean = false,
    var skipBuild: Boolean = false,

    // controls whether to use the build screen
    var runBuild: Boolean = false,
    var runRun: Boolean = false,
    var cleanAndRun: Boolean = false,
    var cleanSingle: Boolean = false,
    var cleanAndRunCmd: MutableList<String> = mutableListOf("play")
)
