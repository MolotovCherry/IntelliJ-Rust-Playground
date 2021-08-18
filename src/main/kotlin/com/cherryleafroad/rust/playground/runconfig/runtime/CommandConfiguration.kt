package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import java.nio.file.Path
import java.nio.file.Paths

data class CommandConfiguration(
        var command: String = "",
        var args: List<String> = listOf(),
        var isPlayRun: Boolean = false,
        // scratch root directory (default cause it's usually this one for play runs)
        var workingDirectory: Path = Paths.get(
            ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
        ),
        var toolchain: RustChannel = RustChannel.DEFAULT,
        // use colors in output
        var processColors: Boolean = true,
        var backtraceMode: BacktraceMode = BacktraceMode.SHORT,
        var env: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    ) {
    fun toRustScratchCommandLine(): RustScratchCommandLine {
        return RustScratchCommandLine(this)
    }
}
