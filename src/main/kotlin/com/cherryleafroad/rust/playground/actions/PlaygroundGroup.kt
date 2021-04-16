package com.cherryleafroad.rust.playground.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import org.rust.cargo.runconfig.hasCargoProject

class PlaygroundGroup : DefaultActionGroup() {
    override fun update(e: AnActionEvent) {
        val project: Project? = e.project
        val hasCargoProject = project?.hasCargoProject
        e.presentation.isEnabledAndVisible = hasCargoProject ?: false
    }
}
