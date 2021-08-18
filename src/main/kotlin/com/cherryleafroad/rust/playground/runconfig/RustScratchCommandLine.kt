package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.runconfig.runtime.PlayConfiguration
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.project.Project
import java.nio.file.Paths

data class RustScratchCommandLine(
    val commandConfiguration: CommandConfiguration,
    val playConfiguration: PlayConfiguration
) {
    constructor(commandConfiguration: CommandConfiguration) : this(commandConfiguration, PlayConfiguration())
    constructor(playConfiguration: PlayConfiguration) : this(
        CommandConfiguration(
            command = "play",
            args = playConfiguration.args,
            isPlayRun = true,
            workingDirectory = Paths.get(ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())),
            processColors = true
        ),
        playConfiguration
    )

    fun run(project: Project, presentableName: String) {
        val runManager = RunManagerEx.getInstanceEx(project)
        val configuration = createRunConfiguration(runManager, presentableName)
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(configuration, executor)
    }

    fun createRunConfiguration(runManager: RunManagerEx, name: String): RunnerAndConfigurationSettings =
        runManager.createRustScratchRunConfiguration(this, name)
}


fun RunManager.createRustScratchRunConfiguration(rustScratchCommandLine: RustScratchCommandLine, name: String): RunnerAndConfigurationSettings {
    val runnerAndConfigurationSettings = createConfiguration(
        name,
        RustScratchConfigurationType.getInstance().factory
    )

    val configuration = runnerAndConfigurationSettings.configuration as RustScratchConfiguration
    configuration.setFromCmd(rustScratchCommandLine)
    return runnerAndConfigurationSettings
}
