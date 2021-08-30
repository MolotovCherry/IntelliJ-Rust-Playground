package com.cherryleafroad.rust.playground.settings

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.intellij.util.xmlb.annotations.Transient

data class ScratchConfiguration(
    var check: Boolean = false,
    var clean: Boolean = false,
    var expand: Boolean = false,
    var infer: Boolean = false,
    var quiet: Boolean = false,
    var release: Boolean = false,
    var test: Boolean = false,
    var verbose: Boolean = false,
    var toolchain: RustChannel = RustChannel.DEFAULT,
    var edition: Edition = Edition.DEFAULT,
    var srcs: List<String> = listOf(),
    var args: List<String> = listOf(),
    var mode: String = "",
    var cargoOptions: List<String> = listOf(),
    var cargoOptionsNoDefault: Boolean = false,

    // the working directory path
    var workingDirectory: String = "",

    // only used for Kargo play
    @Transient
    var kommand: String = "",

    // used for runtime passing of the last generated string
    // so this one won't be serialized
    @Transient
    var generatedArgs: List<String> = listOf(),

    // pass whether direct run or not for Kargo Play
    @Transient
    var directRun: Boolean = false,

    // the sources used for the filters
    @Transient
    var filterSrcs: List<String> = listOf()
)
