package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.parser.ParserResults
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.project.Project


data class RustScratchCommandLine(
    val parserResults: ParserResults,
    val environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
) {

    constructor(
        command: List<String>,
        isPlayRun: Boolean = false,
        environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    ) : this(ParserResults(
        finalCmd = command,
        isPlayRun = isPlayRun,
    ), environmentVariables)

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
