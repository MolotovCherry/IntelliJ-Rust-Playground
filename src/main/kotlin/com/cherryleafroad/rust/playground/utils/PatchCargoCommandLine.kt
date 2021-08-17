package com.cherryleafroad.rust.playground.utils

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.project.Project
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.runconfig.command.CargoCommandConfigurationType
import org.rust.cargo.runconfig.hasCargoProject
import org.rust.cargo.toolchain.BacktraceMode
import org.rust.cargo.toolchain.RustChannel
import org.rust.cargo.toolchain.tools.cargo
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

abstract class RsPatchCommandLineBase {
    abstract val command: String
    abstract val workingDirectory: Path
    abstract val redirectInputFrom: File?
    abstract val additionalArguments: List<String>

    protected abstract fun createRunConfiguration(runManager: RunManagerEx, name: String? = null): RunnerAndConfigurationSettings

    fun run(project: Project, presentableName: String = command) {
        val runManager = RunManagerEx.getInstanceEx(project)
        val configuration = createRunConfiguration(runManager, presentableName)
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(configuration, executor)
    }
}


fun installBinaryCrate(project: Project, crateName: String) {
    project.toolchain?.let {
        val cwd = it.cargo().executable.parent

        val commandLine = PatchCargoCommandLine(
            "install",
            workingDirectory = cwd,
            additionalArguments = listOf("--force", crateName),
        )
        commandLine.run(project, "Install $crateName")
    }
}

data class PatchCargoCommandLine(
    override val command: String, // Can't be `enum` because of custom subcommands
    override val workingDirectory: Path, // Note that working directory selects Cargo project as well
    override val additionalArguments: List<String> = emptyList(),
    override val redirectInputFrom: File? = null,
    val backtraceMode: BacktraceMode = BacktraceMode.DEFAULT,
    val channel: RustChannel = RustChannel.DEFAULT,
    val environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
    val requiredFeatures: Boolean = true,
    val allFeatures: Boolean = false,
    val emulateTerminal: Boolean = false
) : RsPatchCommandLineBase() {
    override fun createRunConfiguration(runManager: RunManagerEx, name: String?): RunnerAndConfigurationSettings =
        runManager.createCargoCommandRunConfiguration(this, name)
}

private fun RunManager.createCargoCommandRunConfiguration(cargoCommandLine: PatchCargoCommandLine, name: String?): RunnerAndConfigurationSettings {
    val runnerAndConfigurationSettings = createConfiguration(
        name ?: cargoCommandLine.command,
        CargoCommandConfigurationType.getInstance().factory
    )
    val configuration = runnerAndConfigurationSettings.configuration as CargoCommandConfiguration
    configuration.setFromCmd(cargoCommandLine)
    return runnerAndConfigurationSettings
}

private fun CargoCommandConfiguration.setFromCmd(cmd: PatchCargoCommandLine) {
    channel = cmd.channel
    // patch to not do \" \" which ruins the options
    val newCmd = mutableListOf(cmd.command)
    newCmd.addAll(cmd.additionalArguments)
    command = newCmd.joinToString(" ")
    // end patch
    requiredFeatures = cmd.requiredFeatures
    allFeatures = cmd.allFeatures
    emulateTerminal = cmd.emulateTerminal
    backtrace = cmd.backtraceMode
    workingDirectory = cmd.workingDirectory
    env = cmd.environmentVariables
    isRedirectInput = cmd.redirectInputFrom != null
}
