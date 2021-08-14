package com.cherryleafroad.rust.playground.actions

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.psiFile

class PlaygroundGroup : DefaultActionGroup() {
    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return

        project.toolchain ?: return

        e.dataContext.psiFile?.virtualFile?.let { file ->
            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)

            if (isRust && isScratch) {
                e.presentation.isEnabledAndVisible = true
            }
        }
    }
}
