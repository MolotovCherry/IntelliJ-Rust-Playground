package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.Helpers
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildConfiguration
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.cherryleafroad.rust.playground.cargo.runconfig.CargoCommandRunner
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildManager
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.cherryleafroad.rust.playground.cargo.toolchain.CargoCommandLine
import com.intellij.icons.AllIcons
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.runconfig.buildtool.CargoBuildResult
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.runconfig.command.CargoCommandConfigurationType
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.document
import org.rust.openapiext.psiFile

class ExecuteAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ActionTools.actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project: Project? = e.project
        e.presentation.isVisible = e.place == ActionPlaces.MAIN_MENU
        e.presentation.isEnabled = false

        if (project != null) {
            e.dataContext.psiFile?.virtualFile?.let {
                if (e.place == ActionPlaces.EDITOR_POPUP || e.place == ActionPlaces.MAIN_MENU || e.place == ActionPlaces.PROJECT_VIEW_POPUP) {
                    val isRust = it.isRustFile
                    val isScratch = ScratchUtil.isScratch(it)

                    e.presentation.isEnabledAndVisible = isRust && isScratch

                    if (e.place == ActionPlaces.MAIN_MENU && !(isRust && isScratch)) {
                        e.presentation.isVisible = true
                    }
                }
            }
        }
    }
}
