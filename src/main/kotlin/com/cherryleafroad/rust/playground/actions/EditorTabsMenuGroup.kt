package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.services.CargoPlayProject
import com.cherryleafroad.rust.playground.services.Settings
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.psiFile

class EditorTabsMenuGroup : DefaultActionGroup("Explore to Cargo Play...", true), DumbAware {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val project: Project = e.project ?: return

        project.toolchain ?: return

        e.dataContext.psiFile?.virtualFile?.let { file ->
            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)

            if (isRust && isScratch) {
                e.presentation.isEnabledAndVisible = true

                val service = CargoPlayProject.getInstance(project)

                val settings = Settings.getInstance().scratches[file.path]
                val srcs = mutableListOf(file.name)
                srcs.addAll(settings.srcs)

                service.setCargoPlayPath(srcs, file.toNioPath().parent.toString())
            }
        }
    }
}
