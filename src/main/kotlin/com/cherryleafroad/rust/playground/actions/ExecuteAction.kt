package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.utils.Helpers
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.psiFile

class ExecuteAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ActionTools.actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project: Project = e.project ?: return
        e.presentation.isEnabledAndVisible = false

        // don't execute on unsupported IDE
        if (Helpers.executionUnsupported(project)) {
            return
        }

        if (project.toolchain != null) {
            e.dataContext.psiFile?.virtualFile?.let {
                if (e.place == ActionPlaces.EDITOR_POPUP ||
                    e.place == ActionPlaces.MAIN_MENU ||
                    e.place == ActionPlaces.PROJECT_VIEW_POPUP ||
                    e.place == ActionPlaces.KEYBOARD_SHORTCUT ||
                    e.place == ActionPlaces.EDITOR_TAB_POPUP) {

                    val isRust = it.isRustFile
                    val isScratch = ScratchUtil.isScratch(it)

                    e.presentation.isEnabledAndVisible = isRust && isScratch
                }
            }
        }
    }
}
