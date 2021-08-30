package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.constants.CargoConstants
import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.runconfig.ui.RustScratchConfigurationEditor
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.PlayRunConfiguration
import com.cherryleafroad.rust.playground.utils.toFile
import com.cherryleafroad.rust.playground.utils.toPath
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ElevationService
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.io.systemIndependentPath
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc
import java.nio.file.Path
import java.nio.file.Paths

@Suppress("PrivatePropertyName")
class RustScratchConfiguration(
    project: Project,
    name: String?,
    factory: ConfigurationFactory
) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
    private val COLOR_ACCEPTING_COMMANDS: List<String> = listOf(
        "bench", "build", "check", "clean", "clippy", "doc", "install", "publish", "run", "rustc", "test", "update"
    )

    // the scratch root directory is always the default working directory
    var commandConfiguration: CommandConfiguration = CommandConfiguration()
    val runConfig: PlayRunConfiguration = Settings.getInstance().runConfigurations[uniqueID]

    // read external run config settings into CommandConfiguration
    init {
        commandConfiguration.command = runConfig.command

        // rebuild args from sources to fix the paths
        runConfig.apply {
            commandConfiguration.args = options + srcs + args
        }

        commandConfiguration.backtraceMode = runConfig.backtraceMode
        commandConfiguration.env = EnvironmentVariablesData.DEFAULT.with(runConfig.env)

        commandConfiguration.workingDirectory = runConfig.workingDirectory
        commandConfiguration.withSudo = runConfig.withSudo
    }

    override fun suggestedName(): String {
        var name = ""
        runConfig.srcs.getOrNull(0)?.let {
            name = it.toFile().name
        }
        return "Play $name"
    }

    fun setFromCmd(cmd: RustScratchCommandLine) {
        commandConfiguration = cmd.commandConfiguration
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RustScratchRunState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RustScratchConfigurationEditor(project)

    fun toGeneralCommandLine(
        project: Project
    ): GeneralCommandLine? {
        val toolchain = project.toolchain ?: return null

        val rustcExecutable = toolchain.rustc().executable.toString()
        val cmdExecutable = if (!commandConfiguration.directRun) {
            toolchain.pathToExecutable(CargoConstants.CARGO)
        } else {
            Paths.get(commandConfiguration.command)
        }

        val params = commandConfiguration.args.toMutableList()

        if (!commandConfiguration.directRun) {
            // first command is cargo subcommand
            params.add(0, commandConfiguration.command)

            // ... but, believe it or not, cargo options come before that

                commandConfiguration.cargoOptions.asReversed().forEach {
                    params.add(0, it)
                }


            // ... and there are others before that!
            if (commandConfiguration.processColors &&
                commandConfiguration.command in COLOR_ACCEPTING_COMMANDS &&
                params.none { it.startsWith("--color")}) {

                params.add(0, "--color=always")
            }

            // ... and what's even more, toolchain comes BEFORE THOSE! Play run configs its own toolchain
            if (commandConfiguration.toolchain != RustChannel.DEFAULT) {
                params.add(0, "+${commandConfiguration.toolchain.channel}")
            }
        }

        val generalCommandLine = GeneralCommandLine(cmdExecutable, commandConfiguration.withSudo)
            .withWorkDirectory(commandConfiguration.workingDirectory.toPath())
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
}

@Suppress("FunctionName", "UnstableApiUsage")
fun GeneralCommandLine(path: Path, withSudo: Boolean = false, vararg args: String) =
    object : GeneralCommandLine(path.systemIndependentPath, *args) {
        override fun createProcess(): Process = if (withSudo) {
            service<ElevationService>().createProcess(this)
        } else {
            super.createProcess()
        }
    }

fun GeneralCommandLine.withWorkDirectory(path: Path?) = withWorkDirectory(path?.systemIndependentPath)
