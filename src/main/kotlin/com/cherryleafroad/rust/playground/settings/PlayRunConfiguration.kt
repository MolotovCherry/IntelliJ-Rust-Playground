package com.cherryleafroad.rust.playground.settings

import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType

// data class for manual run configurations
data class PlayRunConfiguration(
    var command: String = "",
    var options: List<String> = listOf(),
    var args: List<String> = listOf(),
    var srcs: List<String> = listOf(),
    var workingDirectory: String = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance()),
    var env: Map<String, String> = mapOf(),
    var backtraceMode: BacktraceMode = BacktraceMode.SHORT,
    var withSudo: Boolean = false,
)
