package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.PlayRunConfiguration
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.xmlb.annotations.Transient

/*
* Configuration for a Command
* Normally it's cargo.exe <cargoOptions> <subcommand> <args>
* but *if you have to* you can override it with a directRun
* which is like a normal cli call
 */
data class CommandConfiguration(
    // cargo subcommand
    var command: String = "",
    // these are placed BEFORE the subcommand
    var cargoOptions: List<String> = listOf(),
    // and all args are placed after it
    var args: List<String> = listOf(),

    var workingDirectory: String = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance()),

    // command is run as normal command, and cargo is unused
    var directRun: Boolean = false,

    // selected toolchain, gets added automatically
    var toolchain: RustChannel = RustChannel.DEFAULT,

    // use colors in output
    var processColors: Boolean = true,

    // env vars
    var backtraceMode: BacktraceMode = BacktraceMode.SHORT,
    var env: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,

    var withSudo: Boolean = false,

    // store the path of the current executing scratch file
    // this is conveniently used to retrieve settings later as needed
    var scratchFile: String = "",

    // is this a manual run configuration?
    var isManualRun: Boolean = false,
    // the sources used for the filters
    var filterSrcs: List<String> = listOf()
) {
    fun toRustScratchCommandLine(): RustScratchCommandLine {
        return RustScratchCommandLine(this)
    }

    fun clone(): CommandConfiguration {
        val other = CommandConfiguration()
        other.command = command
        other.cargoOptions = cargoOptions
        other.args = args
        other.workingDirectory = workingDirectory
        other.directRun = directRun
        other.toolchain  = toolchain
        other.processColors = processColors
        other.backtraceMode = backtraceMode
        other.env = env
        other.withSudo = withSudo
        other.scratchFile = scratchFile
        other.isManualRun = isManualRun
        other.filterSrcs = filterSrcs
        return other
    }

    fun fromRunConfiguration(config: PlayRunConfiguration) {
        command = config.command
        args = config.options + config.srcs + config.args
        workingDirectory = config.workingDirectory
        env = EnvironmentVariablesData.DEFAULT.with(config.env)
        backtraceMode = config.backtraceMode
        withSudo = config.withSudo
        filterSrcs = config.srcs
        isManualRun = true
    }

    companion object {
        fun fromScratch(file: VirtualFile): CommandConfiguration {
            val cmdConfig = CommandConfiguration()

            val settings = Settings.getInstance().scratches[file.path]
            val pluginSettings = Settings.getInstance().plugin

            if (pluginSettings.kargoPlay) {
                // kargo play run
                cmdConfig.directRun = settings.directRun

                if (!cmdConfig.directRun) {
                    cmdConfig.toolchain = settings.toolchain
                    cmdConfig.command = settings.kommand
                    cmdConfig.cargoOptions = settings.cargoOptions
                    cmdConfig.args = settings.args
                    cmdConfig.workingDirectory = settings.workingDirectory
                } else {
                    // kargo play direct exe run
                    cmdConfig.command = settings.kommand
                    cmdConfig.args = settings.args
                }
            } else {
                // normal cargo play run
                cmdConfig.command = "play"
                cmdConfig.args = settings.generatedArgs
                // scratch root directory
                cmdConfig.workingDirectory = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
            }

            cmdConfig.filterSrcs = settings.filterSrcs
            cmdConfig.scratchFile = file.path

            return cmdConfig
        }
    }
}
