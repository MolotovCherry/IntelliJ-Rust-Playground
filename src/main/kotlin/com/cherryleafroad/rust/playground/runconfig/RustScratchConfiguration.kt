package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.constants.CargoConstants
import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.runconfig.runtime.PlayConfiguration
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.ui.RustScratchConfigurationEditor
import com.cherryleafroad.rust.playground.utils.readString
import com.cherryleafroad.rust.playground.utils.writeString
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.io.systemIndependentPath
import org.jdom.Element
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc

class RustScratchConfiguration(
    project: Project,
    name: String,
    factory: ConfigurationFactory
) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
    private val COLOR_ACCEPTING_COMMANDS: List<String> = listOf(
        "bench", "build", "check", "clean", "clippy", "doc", "install", "publish", "run", "rustc", "test", "update"
    )

    // the scratch root directory is always the default working directory
    var playConfiguration: PlayConfiguration? = null
    var commandConfiguration: CommandConfiguration = CommandConfiguration()

    override fun suggestedName(): String =
        commandConfiguration.command.capitalize() + commandConfiguration.args.joinToString(" ").substringBefore(" ").capitalize()

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.writeString("command",
            "${commandConfiguration.command} ${commandConfiguration.args.joinToString(" ")}"
        )
        commandConfiguration.env.writeExternal(element)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.readString("command")?.let {
            commandConfiguration.command = it.substringBefore(" ")
            commandConfiguration.args = it.substringAfter(" ").split(" ")
        }
        commandConfiguration.env = EnvironmentVariablesData.readExternal(element)
    }

    fun setFromCmd(cmd: RustScratchCommandLine) {
        commandConfiguration = cmd.commandConfiguration
        playConfiguration = cmd.playConfiguration
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RustScratchRunState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RustScratchConfigurationEditor()

    fun toGeneralCommandLine(
        project: Project
    ): GeneralCommandLine? {
        val toolchain = project.toolchain

        if (toolchain != null) {
            val rustcExecutable = toolchain.rustc().executable.toString()
            val cargoExecutable = toolchain.pathToExecutable(CargoConstants.CARGO)

            val params = commandConfiguration.args.toMutableList()

            if (commandConfiguration.processColors && !commandConfiguration.isPlayRun &&
                commandConfiguration.command in COLOR_ACCEPTING_COMMANDS &&
                params.none { it.startsWith("--color") }) {

                params.add(0, "--color=always")
            }

            // true first command is cargo executable, so we technically need this as a param
            params.add(0, commandConfiguration.command)

            val generalCommandLine = GeneralCommandLine(cargoExecutable.systemIndependentPath)
                .withWorkDirectory(commandConfiguration.workingDirectory.systemIndependentPath)
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
