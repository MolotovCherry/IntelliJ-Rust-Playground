package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.Helpers
import com.cherryleafroad.rust.playground.PatchCargoCommandLine
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import org.rust.cargo.project.model.cargoProjects
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.document
import org.rust.openapiext.psiFile

class RunAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!

        val doc = event.dataContext.psiFile?.virtualFile ?: return

        // this could execute with a shortcut for example
        val isRust = doc.isRustFile
        val isScratch = ScratchUtil.isScratch(doc)
        // this COULD trigger if you do a shortcut for example
        if (!isRust || !isScratch) {
            return
        }

        val cargoPlayInstalled = Helpers.checkCargoPlayInstalled(project)
        if (cargoPlayInstalled) {
            val code = doc.document!!.text
            val cargoProject = project.cargoProjects.allProjects.firstOrNull() ?: return

            val cwd = doc.toNioPath().parent
            val fileName = doc.name

            val (args, toolchain) = Helpers.parseScratch(fileName, code)

            val commandLine = PatchCargoCommandLine(
                "play",
                cwd,
                args,
                channel = toolchain
            )
            commandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
        } else {
            Helpers.cargoPlayInstallNotification(project)
        }
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
