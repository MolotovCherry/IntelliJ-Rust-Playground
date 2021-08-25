package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.project.Project

// This class is not really meant to be instantiated directly
data class RustScratchCommandLine(
    val commandConfiguration: CommandConfiguration
) {
    fun run(project: Project, presentableName: String) {
        val runManager = RunManagerEx.getInstanceEx(project)
        val configuration = createRunConfiguration(runManager, presentableName)
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        ProgramRunnerUtil.executeConfiguration(configuration, executor)
    }

    private fun createRunConfiguration(runManager: RunManagerEx, name: String): RunnerAndConfigurationSettings =
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
