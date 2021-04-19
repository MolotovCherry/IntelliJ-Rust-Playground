/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.cargo.runconfig

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import org.rust.cargo.runconfig.RsDefaultProgramRunnerBase
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildManager.getBuildConfiguration
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildManager.isBuildConfiguration
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildManager.isBuildToolWindowEnabled
import org.rust.cargo.runconfig.command.CargoCommandConfiguration

open class CargoCommandRunner : RsDefaultProgramRunnerBase() {
    override fun getRunnerId(): String = RUNNER_ID

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (executorId != DefaultRunExecutor.EXECUTOR_ID || profile !is CargoCommandConfiguration) return false
        val cleaned = profile.clean().ok ?: return false
        return profile.project.isBuildToolWindowEnabled ||
            cleaned.cmd.command != "test" ||
            getBuildConfiguration(profile) == null
    }

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        val configuration = environment.runProfile
        return if (configuration is CargoCommandConfiguration &&
            !(isBuildConfiguration(configuration) && configuration.project.isBuildToolWindowEnabled)) {
            super.doExecute(state, environment)
        } else {
            null
        }
    }

    companion object {
        const val RUNNER_ID: String = "CargoCommandRunner"
    }
}
