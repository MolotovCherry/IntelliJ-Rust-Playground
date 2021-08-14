package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.scratch.ui.ScratchSettings
import com.cherryleafroad.rust.playground.services.CargoPlayProjectService
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.hasCargoProject
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.psiFile

class EditorTabsMenuGroup : DefaultActionGroup("Explore to Cargo Play...", true), DumbAware {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val project: Project = e.project ?: return
        val hasCargoProject = project.hasCargoProject

        if (!hasCargoProject || project.toolchain == null) {
            return
        }

        e.dataContext.psiFile?.virtualFile?.let { file ->
            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)

            if (isRust && isScratch) {
                e.presentation.isEnabled = true
                e.presentation.isVisible = true

                val service = project.service<CargoPlayProjectService>()

                val settings = ScratchSettings(file)
                val srcs = mutableListOf(file.name)
                srcs.addAll(settings.SRC.getValue().split(" ").filter { it.isNotEmpty() })

                service.setCargoPlayPath(srcs, file.toNioPath().parent.toString())
            }
        }
    }
}
