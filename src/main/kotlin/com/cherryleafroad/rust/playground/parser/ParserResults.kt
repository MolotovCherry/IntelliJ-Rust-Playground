package com.cherryleafroad.rust.playground.parser

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import java.nio.file.Path
import java.nio.file.Paths

enum class RustChannel(val index: Int, val channel: String?) {
    DEFAULT(0, null),
    STABLE(1, "stable"),
    BETA(2, "beta"),
    NIGHTLY(3, "nightly"),
    DEV(4, "dev");

    override fun toString(): String = channel ?: "[default]"

    companion object {
        fun fromIndex(index: Int): RustChannel = values().find { it.index == index } ?: DEFAULT
    }
}

enum class Edition(val index: Int, val myName: String) {
    DEFAULT(0, "DEFAULT"),
    EDITION_2015(1, "2015"),
    EDITION_2018(2, "2018");
    //EDITION_2021(3, "2021"); -> not yet released

    companion object {
        fun fromIndex(index: Int): Edition = values().find { it.index == index } ?: DEFAULT
    }
}

enum class BacktraceMode(val index: Int, val title: String) {
    NO(0, "No"),
    SHORT(1, "Short"),
    FULL(2, "Full");

    override fun toString(): String = title

    companion object {
        @JvmField
        val DEFAULT: BacktraceMode = SHORT

        fun fromIndex(index: Int): BacktraceMode = values().find { it.index == index } ?: DEFAULT
    }
}

data class ParserResults(
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
    var args: List<String> = listOf(),

    var runCmd: List<String> = listOf(),
    var finalCmd: List<String> = listOf(),

    // scratch root directory
    var workingDirectory: Path = Paths.get(ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())),
    var backtraceMode: BacktraceMode = BacktraceMode.SHORT
)
