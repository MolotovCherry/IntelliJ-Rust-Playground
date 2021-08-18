package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.constants.CargoConstants
import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.runconfig.runtime.PlayConfiguration
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.runconfig.ui.RustScratchConfigurationEditor
import com.cherryleafroad.rust.playground.utils.*
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ElevationService
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.io.systemIndependentPath
import org.jdom.Element
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc
import java.nio.file.Path

class RustScratchConfiguration(
    project: Project,
    name: String,
    factory: ConfigurationFactory
) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
    private val COLOR_ACCEPTING_COMMANDS: List<String> = listOf(
        "bench", "build", "check", "clean", "clippy", "doc", "install", "publish", "run", "rustc", "test", "update"
    )

    // the scratch root directory is always the default working directory
    var playConfiguration: PlayConfiguration = PlayConfiguration()
    var commandConfiguration: CommandConfiguration = CommandConfiguration()

    override fun suggestedName(): String =
        "${commandConfiguration.command.capitalize()} ${commandConfiguration.runtime.sources.joinToString(" ").substringBefore(" ")}"

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.writeString("command", commandConfiguration.command)
        element.writeString("args", commandConfiguration.args.joinToString(" "))

        element.writeBool("isFromRun", commandConfiguration.isFromRun)
        element.writeString("options", commandConfiguration.runtime.options.joinToString(" "))
        element.writeString("sources", commandConfiguration.runtime.sources.joinToString(" "))
        element.writeString("runtimeArgs", commandConfiguration.runtime.args.joinToString(" "))

        element.writeBool("isPlayRun", commandConfiguration.isPlayRun)
        element.writeEnum("backtraceMode", commandConfiguration.backtraceMode)
        element.writeEnum("edition", commandConfiguration.edition)
        element.writeEnum("channel", commandConfiguration.channel)

        commandConfiguration.env.writeExternal(element)

        element.writePath("workingDirectory", commandConfiguration.workingDirectory)
        element.writeBool("withSudo", commandConfiguration.withSudo)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.readString("command")?.let { commandConfiguration.command = it }
        element.readString("args")?.let { commandConfiguration.args = it.split(" ") }

        element.readBool("isFromRun")?.let { commandConfiguration.isFromRun = it }
        element.readString("options")?.let { commandConfiguration.runtime.options = it.split(" ") }
        element.readString("sources")?.let { commandConfiguration.runtime.sources = it.split(" ") }
        element.readString("runtimeArgs")?.let { commandConfiguration.runtime.args = it.split(" ") }

        element.readBool("isPlayRun")?.let { commandConfiguration.isPlayRun = it }
        element.readEnum<BacktraceMode>("backtraceMode")?.let { commandConfiguration.backtraceMode = it }
        element.readEnum<Edition>("edition")?.let { commandConfiguration.edition = it }
        element.readEnum<RustChannel>("channel")?.let { commandConfiguration.channel = it }
        commandConfiguration.env = EnvironmentVariablesData.readExternal(element)

        element.readPath("workingDirectory")?.let { commandConfiguration.workingDirectory = it }
        element.readBool("withSudo")?.let { commandConfiguration.withSudo = it }
    }

    fun setFromCmd(cmd: RustScratchCommandLine) {
        commandConfiguration = cmd.commandConfiguration
        playConfiguration = cmd.playConfiguration
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RustScratchRunState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RustScratchConfigurationEditor(project)

    fun toGeneralCommandLine(
        project: Project
    ): GeneralCommandLine? {
        val toolchain = project.toolchain

        if (toolchain != null) {
            val rustcExecutable = toolchain.rustc().executable.toString()
            val cargoExecutable = toolchain.pathToExecutable(CargoConstants.CARGO)

            val params = commandConfiguration.args.toMutableList()

            if (commandConfiguration.processColors && !commandConfiguration.isPlayRun &&
                !commandConfiguration.isFromRun &&
                commandConfiguration.command in COLOR_ACCEPTING_COMMANDS &&
                params.none { it.startsWith("--color")}) {

                params.add(0, "--color=always")
            }

            // true first command is cargo executable, so we technically need this as a param
            params.add(0, commandConfiguration.command)

            val generalCommandLine = GeneralCommandLine(cargoExecutable, commandConfiguration.withSudo)
                .withWorkDirectory(commandConfiguration.workingDirectory)
                .withEnvironment("TERM", "ansi")
                .withParameters(params)
                .withCharset(Charsets.UTF_8)
                .withRedirectErrorStream(true)
                .withEnvironment("RUSTC", rustcExecutable)

            when (commandConfiguration.backtraceMode) {
                BacktraceMode.SHORT -> generalCommandLine.withEnvironment(CargoConstants.RUST_BACKTRACE_ENV_VAR, "short")
                BacktraceMode.FULL -> generalCommandLine.withEnvironment(CargoConstants.RUST_BACKTRACE_ENV_VAR, "full")
                BacktraceMode.NO -> Unit
            }

            commandConfiguration.env.configureCommandLine(generalCommandLine, true)

            return generalCommandLine
        }

        return null
    }
}

@Suppress("FunctionName", "UnstableApiUsage")
fun GeneralCommandLine(path: Path, withSudo: Boolean = false, vararg args: String) =
    object : GeneralCommandLine(path.systemIndependentPath, *args) {
        override fun createProcess(): Process = if (withSudo) {
            ElevationService.getInstance().createProcess(this)
        } else {
            super.createProcess()
        }
    }

fun GeneralCommandLine.withWorkDirectory(path: Path?) = withWorkDirectory(path?.systemIndependentPath)
