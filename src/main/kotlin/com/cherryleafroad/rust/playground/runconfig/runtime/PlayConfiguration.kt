package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel

data class PlayConfiguration(
    var check: Boolean = false,
    var clean: Boolean = false,
    var expand: Boolean = false,
    var infer: Boolean = false,
    var quiet: Boolean = false,
    var release: Boolean = false,
    var test: Boolean = false,
    var verbose: Boolean = false,
    var toolchain: RustChannel = RustChannel.DEFAULT,

    var cargoOption: List<String> = listOf(),
    var edition: Edition = Edition.DEFAULT,
    var mode: String = "",

    // args to pass various parts
    var src: List<String> = listOf(),

    // cargo play args
    var args: List<String> = listOf()
) {
    fun toRustScratchCommandLine(): RustScratchCommandLine {
        return RustScratchCommandLine(this)
    }
}
